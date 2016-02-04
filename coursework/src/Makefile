# from http://stackoverflow.com/a/20427665
PKG = hu/devo/aad
SORUCE = src/$(PKG)
DEST = out
JFLAGS = -g -d $(DEST) -sourcepath $(SORUCE)

JAVA_FILES:=$(wildcard $(SORUCE)/*.java)

JAVA_CLASSES:=$(patsubst %.java,%.class,$(JAVA_FILES))

.PHONY: classes
LIST:= 


classes: $(JAVA_CLASSES)
		javac $(JFLAGS) $(LIST)

MAIN = Main
run: classes
	java -cp $(DEST) $(PKG)/$(MAIN)

$(JAVA_CLASSES) : %.class : %.java
		$(eval LIST+=$$<)
