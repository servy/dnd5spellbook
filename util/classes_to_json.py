import os
import codecs
import shutil

from html.parser import HTMLParser

classes = []

class Class:
	def __init__(self, name=None):
		self.name = name
		self.spells = []
	def toJson(self):
		return """ {
			"name": "%s",
			"spells": [%s]
		}
		""" % (self.name, ', '.join(self.spells))


class ClassXMLParser(HTMLParser):
	def isCastingTime(self, tag):
		return tag == 'b' and self.bCounter == 0
	def isRange(self, tag):
		return tag == 'b' and self.bCounter == 1
	def isComponents(self, tag):
		return tag == 'b' and self.bCounter == 2
	def isDuration(self, tag):
		return tag == 'b' and self.bCounter == 3
	def isDescription(self, tag):
		return tag == 'article'	


	def handle_starttag(self, tag, attrs):
		if tag == 'class':
			self.state = 'READING_CLASS'
			self.newClass = Class(attrs[0][1])
		elif tag == 'item':
			self.state = 'READING_ITEM'

	def handle_endtag(self, tag):
		if tag == 'class':
			self.state = ''
			self.classes.append(self.newClass)
			self.items = []			
		elif tag == 'item':
			self.state = ''
			self.newClass.spells.append(self.currentItem)

	def handle_data(self, data):
		if self.state == 'READING_ITEM':
			self.currentItem = '"' + data + '"'

	def __init__(self):
		super().__init__()
		self.reset()
		self.fed = []
		self.classes = []
		self.state = ''
		self.items = []



with open('spell_metadata.xml', encoding='utf8') as f:
	dndclass = Class()
	parser = ClassXMLParser()
	parser.feed(f.read())
	classes = parser.classes

with codecs.open('classes.json', 'w', 'utf8') as f:
	f.write('[\n')	
	delimiter = ''
	for dndclass in classes:
		f.write(delimiter)	
		f.write(dndclass.toJson())
		delimiter = ', '
	f.write('\n]')

