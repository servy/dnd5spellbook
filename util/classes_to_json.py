import os
import codecs
import shutil

from html.parser import HTMLParser

classes = []

def getSpellsByLevels(spellsByLevels):
	result = []
	for i, spells in enumerate(spellsByLevels):
		result.append(
		"""{
				"level": %s,
				"spells": [%s]
			}
		""" % (i, ', '.join(spells))
		)
	return result

class Class:
	def __init__(self, name=None):
		self.name = name
		self.spellsByLevels = []
	def toJson(self):
		return """ {
			"name": "%s",
			"spellsByLevels": [%s]
		}
		""" % (self.name, ', '.join(getSpellsByLevels(self.spellsByLevels)))

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
		elif tag == 'level':
			self.currentSpellLevel = attrs[0][1];
	def handle_endtag(self, tag):
		if tag == 'class':
			self.state = ''
			self.classes.append(self.newClass)			
		elif tag == 'item':
			self.state = ''
			self.items.append(self.currentItem)
		elif tag == 'level':
			self.newClass.spellsByLevels.append(self.items)
			self.items = []

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