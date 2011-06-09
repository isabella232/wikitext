WikiText Library
================

[WikiText 1.4](http://wikitext.fusesource.org/maven/1.4/) released 2011-06-09
----

* fixed tables to allow nested formatting (e.g. lists inside table cells)
* enabled nesting tables inside list items
* fixed ant tasks, so that they now build properly

[WikiText 1.3](http://wikitext.fusesource.org/maven/1.3/), released 2011-02-10
----

* confluence fix: when using simple anchors, make sure the pound sign is not included in the final text
* confluence fix: fix tables with no headers row
* confluence fix: fix phrase modifiers to be correctly delimited by non-words characters
* confluence fix: make sure hyperlinks can be escaped

[WikiText 1.2](http://wikitext.fusesource.org/maven/1.2/), released 2011-02-07
----

* fixed OSGi metadata
* include the language bundles that are part of the source.
* confluence fix: make tables behave like confluence tables (add a wrapper div, use confluence css class names)
* confluence fix: list parsing (lists are closed way to often and embedded tables creates new lists)
* confluence fix: {link} parsing

[WikiText 1.1](http://wikitext.fusesource.org/maven/1.1/), released 2010-10-27
----

* Enabling the test module so we get some unit test coverage.
* Major improvements to a table's nested block parsing capability. It should now be able to cope with almost any (sensible) wikitext that you throw at it. In particular, you can now put code blocks into table cells.
* Modified the ListBlock implementation to enable nested blocks inside list items. For example, code listings inside list items. Got rid of the annoying extra blank line at the end of code listings. Set .gitignore to ignore Eclipse project files.
* Added beginLink() and endLink() method impls (enables cleaner implementation of HyperlinkPhraseModifier) and changed mapping of BlockType.CODE to pre element.
* Changed TableBlock class to use java.util.logging package (instead of log4j). This cures the problem of a missing dependency when building scalate-wikitext.

[WikiText 1.0](http://wikitext.fusesource.org/maven/1.0/), released 2010-09-14
----

* Initial release
