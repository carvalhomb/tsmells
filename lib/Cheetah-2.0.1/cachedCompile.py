from Cheetah.Template import Template
source = file('/home/tavis/cvs_working/Cheetah/src/Templates/SkeletonPage.tmpl').read()
##klass = Template.compile(source,
##                         cacheCompilationResults=1,
##                         useCache=1,
##                             )
for i in range(2000):
    klass = Template.compile(source,
                             cacheCompilationResults=0,
                             useCache=0,
                             )
