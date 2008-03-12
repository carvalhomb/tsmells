from Cheetah.Template import Template
import gc

src = open('/tmp/z.py').read()
tclass = Template.compile(src)
#t = Template(src)
nr = 0
while True:
    #tclass = Template.compile(src)
    #t = tclass()
    t = Template(src)
    
    output = t.respond()
    
    nr += 1
    if not nr % 10000:
        print 
        #print 'collect'
	#gc.collect()

        print 'tclass id', id(t.__class__)
        print 'cache size', len(Template._CHEETAH_compileCache.keys())
