# Define variables
JAVAC = javac
JAVA = java
MAIN_CLASS = RecSPLMain
SRC_DIR = src
BIN_DIR = bin

# Default target
.PHONY: all
all: clean compile run

# Compile all .java files
.PHONY: compile
compile:
	@mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) *.java

# Run the main class
.PHONY: run
run:
	$(JAVA) -cp $(BIN_DIR) $(MAIN_CLASS)

# Clean all .class files
.PHONY: clean
clean:
	@rm -rf $(BIN_DIR)