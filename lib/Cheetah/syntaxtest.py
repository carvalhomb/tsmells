from Cheetah.Template import Template


print Template("""
#compiler useNameMapper=False

#for i an range(10)
  $i
#end for





""")
