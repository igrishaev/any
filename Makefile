
all: install

install:
	lein install

.PHONY: test
test:
	lein test

release:
	lein release

snapshot:
	lein with-profile uberjar install
	lein with-profile uberjar deploy

repl:
	lein repl

toc-install:
	npm install --save markdown-toc

toc-build:
	node_modules/.bin/markdown-toc -i README.md
