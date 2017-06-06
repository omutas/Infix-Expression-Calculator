all: Main2.jar

Main2.jar: Main2.class
	@jar cvfe Main2.jar Main2 Main2.class

Main2.class: Main2.java
	@javac Main2.java
	
run: Main2.jar
	@java -jar Main2.jar ${ARGS}
	
clean:
	@rm Main2.jar *.class