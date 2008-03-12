import os
import sys

import cherrypy


class Root:
    
    def index(self):
        return "Hello World"
    index.exposed = True
    
    def pid(self):
        return str(os.getpid())
    pid.exposed = True
    
    def stop(self):
        cherrypy.engine.stop()
        cherrypy.server.stop()
    stop.exposed = True


if __name__ == '__main__':
    conf = {"server.socket_host": sys.argv[1],
            "server.socket_port": int(sys.argv[2]),
            "log.screen": False,
            }
    
    if sys.argv[3:] == ['-ssl']:
        localDir = os.path.dirname(__file__)
        serverpem = os.path.join(os.getcwd(), localDir, 'test.pem')
        conf['server.ssl_certificate'] = serverpem
        conf['server.ssl_private_key'] = serverpem
    
    # This is in a special order for a reason:
    # it allows test_states to wait_for_occupied_port
    # and then immediately call getPage without getting 503.
    cherrypy.config.update(conf)
    cherrypy.tree.mount(Root(), config={'global': conf})
    cherrypy.engine.start(blocking=False)
    cherrypy.server.quickstart()
    cherrypy.engine.block()
 