from cherrypy.test import test
test.prefer_parent_path()

import httplib
import socket
import sys
import time
timeout = 0.1


import cherrypy
from cherrypy.test import webtest


pov = 'pPeErRsSiIsStTeEnNcCeE oOfF vViIsSiIoOnN'

def setup_server():
    class Root:
        
        def index(self):
            return pov
        index.exposed = True
        page1 = index
        page2 = index
        page3 = index
        
        def hello(self):
            return "Hello, world!"
        hello.exposed = True
        
        def stream(self, set_cl=False):
            if set_cl:
                cherrypy.response.headers['Content-Length'] = 10
            
            def content():
                for x in xrange(10):
                    yield str(x)
            
            return content()
        stream.exposed = True
        stream._cp_config = {'response.stream': True}
        
        def upload(self):
            return ("thanks for '%s' (%s)" %
                    (cherrypy.request.body.read(),
                     cherrypy.request.headers['Content-Type']))
        upload.exposed = True
        
        def custom(self, response_code):
            cherrypy.response.status = response_code
            return "Code = %s" % response_code
        custom.exposed = True
    
    cherrypy.tree.mount(Root())
    cherrypy.config.update({
        'server.max_request_body_size': 100,
        'environment': 'test_suite',
        })


from cherrypy.test import helper

class ConnectionTests(helper.CPWebCase):
    
    def test_HTTP11(self):
        if cherrypy.server.protocol_version != "HTTP/1.1":
            print "skipped ",
            return
        
        self.PROTOCOL = "HTTP/1.1"
        
        self.persistent = True
        
        # Make the first request and assert there's no "Connection: close".
        self.getPage("/")
        self.assertStatus('200 OK')
        self.assertBody(pov)
        self.assertNoHeader("Connection")
        
        # Make another request on the same connection.
        self.getPage("/page1")
        self.assertStatus('200 OK')
        self.assertBody(pov)
        self.assertNoHeader("Connection")
        
        # Test client-side close.
        self.getPage("/page2", headers=[("Connection", "close")])
        self.assertStatus('200 OK')
        self.assertBody(pov)
        self.assertHeader("Connection", "close")
        
        # Make another request on the same connection, which should error.
        self.assertRaises(httplib.NotConnected, self.getPage, "/")
    
    def test_Streaming_no_len(self):
        self._streaming(set_cl=False)
    
    def test_Streaming_with_len(self):
        self._streaming(set_cl=True)
    
    def _streaming(self, set_cl):
        if cherrypy.server.protocol_version == "HTTP/1.1":
            self.PROTOCOL = "HTTP/1.1"
            
            self.persistent = True
            
            # Make the first request and assert there's no "Connection: close".
            self.getPage("/")
            self.assertStatus('200 OK')
            self.assertBody(pov)
            self.assertNoHeader("Connection")
            
            # Make another, streamed request on the same connection.
            if set_cl:
                # When a Content-Length is provided, the content should stream
                # without closing the connection.
                self.getPage("/stream?set_cl=Yes")
                self.assertHeader("Content-Length")
                self.assertNoHeader("Connection", "close")
                self.assertNoHeader("Transfer-Encoding")
                
                self.assertStatus('200 OK')
                self.assertBody('0123456789')
            else:
                # When no Content-Length response header is provided,
                # streamed output will either close the connection, or use
                # chunked encoding, to determine transfer-length.
                self.getPage("/stream")
                self.assertNoHeader("Content-Length")
                self.assertStatus('200 OK')
                self.assertBody('0123456789')
                
                chunked_response = False
                for k, v in self.headers:
                    if k.lower() == "transfer-encoding":
                        if str(v) == "chunked":
                            chunked_response = True
                
                if chunked_response:
                    self.assertNoHeader("Connection", "close")
                else:
                    self.assertHeader("Connection", "close")
                    
                    # Make another request on the same connection, which should error.
                    self.assertRaises(httplib.NotConnected, self.getPage, "/")
        else:
            self.PROTOCOL = "HTTP/1.0"
            
            self.persistent = True
            
            # Make the first request and assert Keep-Alive.
            self.getPage("/", headers=[("Connection", "Keep-Alive")])
            self.assertStatus('200 OK')
            self.assertBody(pov)
            self.assertHeader("Connection", "Keep-Alive")
            
            # Make another, streamed request on the same connection.
            if set_cl:
                # When a Content-Length is provided, the content should
                # stream without closing the connection.
                self.getPage("/stream?set_cl=Yes",
                             headers=[("Connection", "Keep-Alive")])
                self.assertHeader("Content-Length")
                self.assertHeader("Connection", "Keep-Alive")
                self.assertNoHeader("Transfer-Encoding")
                self.assertStatus('200 OK')
                self.assertBody('0123456789')
            else:
                # When a Content-Length is not provided,
                # the server should close the connection.
                self.getPage("/stream", headers=[("Connection", "Keep-Alive")])
                self.assertStatus('200 OK')
                self.assertBody('0123456789')
                
                self.assertNoHeader("Content-Length")
                self.assertNoHeader("Connection", "Keep-Alive")
                self.assertNoHeader("Transfer-Encoding")
                
                # Make another request on the same connection, which should error.
                self.assertRaises(httplib.NotConnected, self.getPage, "/")
    
    def test_HTTP11_Timeout(self):
        if cherrypy.server.protocol_version != "HTTP/1.1":
            print "skipped ",
            return
        
        old_timeout = None
        try:
            httpserver = cherrypy.server.httpservers.keys()[0]
            old_timeout = httpserver.timeout
        except (AttributeError, IndexError):
            print "skipped ",
            return
        
        try:
            httpserver.timeout = timeout
            self.PROTOCOL = "HTTP/1.1"
            
            # Make an initial request
            self.persistent = True
            conn = self.HTTP_CONN
            conn.putrequest("GET", "/", skip_host=True)
            conn.putheader("Host", self.HOST)
            conn.endheaders()
            response = conn.response_class(conn.sock, method="GET")
            response.begin()
            self.assertEqual(response.status, 200)
            self.body = response.read()
            self.assertBody(pov)
            
            # Make a second request on the same socket
            conn._output('GET /hello HTTP/1.1')
            conn._output("Host: %s" % self.HOST)
            conn._send_output()
            response = conn.response_class(conn.sock, method="GET")
            response.begin()
            self.assertEqual(response.status, 200)
            self.body = response.read()
            self.assertBody("Hello, world!")
            
            # Wait for our socket timeout
            time.sleep(timeout * 2)
            
            # Make another request on the same socket, which should error
            conn._output('GET /hello HTTP/1.1')
            conn._output("Host: %s" % self.HOST)
            conn._send_output()
            response = conn.response_class(conn.sock, method="GET")
            try:
                response.begin()
            except:
                if not isinstance(sys.exc_info()[1],
                                  (socket.error, httplib.BadStatusLine)):
                    self.fail("Writing to timed out socket didn't fail"
                              " as it should have: %s" % sys.exc_info()[1])
            else:
                self.fail("Writing to timed out socket didn't fail"
                          " as it should have: %s" %
                          response.read())
            
            conn.close()
            
            # Make another request on a new socket, which should work
            self.persistent = True
            conn = self.HTTP_CONN
            conn.putrequest("GET", "/", skip_host=True)
            conn.putheader("Host", self.HOST)
            conn.endheaders()
            response = conn.response_class(conn.sock, method="GET")
            response.begin()
            self.assertEqual(response.status, 200)
            self.body = response.read()
            self.assertBody(pov)
        finally:
            if old_timeout is not None:
                httpserver.timeout = old_timeout
    
    def test_HTTP11_pipelining(self):
        if cherrypy.server.protocol_version != "HTTP/1.1":
            print "skipped ",
            return
        
        self.PROTOCOL = "HTTP/1.1"
        
        # Test pipelining. httplib doesn't support this directly.
        self.persistent = True
        conn = self.HTTP_CONN
        
        # Put request 1
        conn.putrequest("GET", "/hello", skip_host=True)
        conn.putheader("Host", self.HOST)
        conn.endheaders()
        
        for trial in xrange(5):
            # Put next request
            conn._output('GET /hello HTTP/1.1')
            conn._output("Host: %s" % self.HOST)
            conn._send_output()
            
            # Retrieve previous response
            response = conn.response_class(conn.sock, method="GET")
            response.begin()
            body = response.read()
            self.assertEqual(response.status, 200)
            self.assertEqual(body, "Hello, world!")
        
        # Retrieve final response
        response = conn.response_class(conn.sock, method="GET")
        response.begin()
        body = response.read()
        self.assertEqual(response.status, 200)
        self.assertEqual(body, "Hello, world!")
        
        conn.close()
    
    def test_100_Continue(self):
        if cherrypy.server.protocol_version != "HTTP/1.1":
            print "skipped ",
            return
        
        self.PROTOCOL = "HTTP/1.1"
        
        self.persistent = True
        conn = self.HTTP_CONN
        
        # Try a page without an Expect request header first.
        # Note that httplib's response.begin automatically ignores
        # 100 Continue responses, so we must manually check for it.
        conn.putrequest("POST", "/upload", skip_host=True)
        conn.putheader("Host", self.HOST)
        conn.putheader("Content-Type", "text/plain")
        conn.putheader("Content-Length", "4")
        conn.endheaders()
        conn.send("d'oh")
        response = conn.response_class(conn.sock, method="POST")
        version, status, reason = response._read_status()
        self.assertNotEqual(status, 100)
        conn.close()
        
        # Now try a page with an Expect header...
        conn.connect()
        conn.putrequest("POST", "/upload", skip_host=True)
        conn.putheader("Host", self.HOST)
        conn.putheader("Content-Type", "text/plain")
        conn.putheader("Content-Length", "17")
        conn.putheader("Expect", "100-continue")
        conn.endheaders()
        response = conn.response_class(conn.sock, method="POST")
        
        # ...assert and then skip the 100 response
        version, status, reason = response._read_status()
        self.assertEqual(status, 100)
        while True:
            skip = response.fp.readline().strip()
            if not skip:
                break
        
        # ...send the body
        conn.send("I am a small file")
        
        # ...get the final response
        response.begin()
        self.status, self.headers, self.body = webtest.shb(response)
        self.assertStatus(200)
        self.assertBody("thanks for 'I am a small file' (text/plain)")
    
    def test_No_Message_Body(self):
        if cherrypy.server.protocol_version != "HTTP/1.1":
            print "skipped ",
            return
        
        self.PROTOCOL = "HTTP/1.1"
        
        # Set our HTTP_CONN to an instance so it persists between requests.
        self.persistent = True
        conn = self.HTTP_CONN
        
        # Make the first request and assert there's no "Connection: close".
        self.getPage("/")
        self.assertStatus('200 OK')
        self.assertBody(pov)
        self.assertNoHeader("Connection")
        
        # Make a 204 request on the same connection.
        self.getPage("/custom/204")
        self.assertStatus(204)
        self.assertNoHeader("Content-Length")
        self.assertBody("")
        self.assertNoHeader("Connection")
        
        # Make a 304 request on the same connection.
        self.getPage("/custom/304")
        self.assertStatus(304)
        self.assertNoHeader("Content-Length")
        self.assertBody("")
        self.assertNoHeader("Connection")
    
    def test_Chunked_Encoding(self):
        if cherrypy.server.protocol_version != "HTTP/1.1":
            print "skipped ",
            return
        
        if (hasattr(self, 'harness') and
            "modpython" in self.harness.__class__.__name__.lower()):
            # mod_python forbids chunked encoding
            print "skipped ",
            return
        
        self.PROTOCOL = "HTTP/1.1"
        
        # Set our HTTP_CONN to an instance so it persists between requests.
        self.persistent = True
        conn = self.HTTP_CONN
        
        # Try a normal chunked request (with extensions)
        body = ("8;key=value\r\nxx\r\nxxxx\r\n5\r\nyyyyy\r\n0\r\n"
                "Content-Type: application/x-json\r\n\r\n")
        conn.putrequest("POST", "/upload", skip_host=True)
        conn.putheader("Host", self.HOST)
        conn.putheader("Transfer-Encoding", "chunked")
        conn.putheader("Trailer", "Content-Type")
        # Note that this is somewhat malformed:
        # we shouldn't be sending Content-Length.
        # RFC 2616 says the server should ignore it.
        conn.putheader("Content-Length", len(body))
        conn.endheaders()
        conn.send(body)
        response = conn.getresponse()
        self.status, self.headers, self.body = webtest.shb(response)
        self.assertStatus('200 OK')
        self.assertBody("thanks for 'xx\r\nxxxxyyyyy' (application/x-json)")
        
        # Try a chunked request that exceeds server.max_request_body_size.
        # Note that the delimiters and trailer are included.
        body = "5f\r\n" + ("x" * 95) + "\r\n0\r\n\r\n"
        conn.putrequest("POST", "/upload", skip_host=True)
        conn.putheader("Host", self.HOST)
        conn.putheader("Transfer-Encoding", "chunked")
        conn.putheader("Content-Type", "text/plain")
##        conn.putheader("Content-Length", len(body))
        conn.endheaders()
        conn.send(body)
        response = conn.getresponse()
        self.status, self.headers, self.body = webtest.shb(response)
        self.assertStatus(413)
        self.assertBody("")
    
    def test_HTTP10(self):
        self.PROTOCOL = "HTTP/1.0"
        if self.scheme == "https":
            self.HTTP_CONN = httplib.HTTPSConnection
        else:
            self.HTTP_CONN = httplib.HTTPConnection
        
        # Test a normal HTTP/1.0 request.
        self.getPage("/page2")
        self.assertStatus('200 OK')
        self.assertBody(pov)
        # Apache, for example, may emit a Connection header even for HTTP/1.0
##        self.assertNoHeader("Connection")
        
        # Test a keep-alive HTTP/1.0 request.
        self.persistent = True
        
        self.getPage("/page3", headers=[("Connection", "Keep-Alive")])
        self.assertStatus('200 OK')
        self.assertBody(pov)
        self.assertHeader("Connection", "Keep-Alive")
        
        # Remove the keep-alive header again.
        self.getPage("/page3")
        self.assertStatus('200 OK')
        self.assertBody(pov)
        # Apache, for example, may emit a Connection header even for HTTP/1.0
##        self.assertNoHeader("Connection")


if __name__ == "__main__":
    setup_server()
    helper.testmain()
