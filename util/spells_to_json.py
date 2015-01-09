import os
import codecs
import shutil

from html.parser import HTMLParser

spells = []

class Spell:
	def __init__(self, name=None):
		self.name = name
		self.description = ''
	def __str__(self):
		return self.name + '\n' + self.type + '\n' + self.castingTime + '\n' + self.range + '\n' + self.components + '\n' + self.duration + '\n' + self.description
	def toJson(self):
		return """ {
			"name": "%s",
			"type": "%s",
			"castingTime": "%s",
			"range": "%s",
			"components": "%s",
			"duration": "%s",
			"description": "%s"
		}
		""" % (self.name, self.type, self.castingTime, self.range, self.components, self.duration, self.description.replace('\n', ''))


class SpellHTMLParser(HTMLParser):
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
		# print("Starting tag  :", tag)	
		if self.state == 'DESCRIPTION':
			if tag == 'b':
				self.state = 'DESCRIPTION_B_TAG_DATA'
			self.spell.description += '<' + tag + '>'
		else:	
			if tag == 'i':
				self.state = 'TYPE_READING'	
			elif self.isDescription(tag):
				self.state = 'DESCRIPTION'
	def handle_endtag(self, tag):
		# print("Ending tag  :", tag)
		if self.isDescription(tag):
			self.state = ''    
		if self.state == 'DESCRIPTION':
			self.spell.description += '</' + tag + '>'
		elif self.state == 'DESCRIPTION_B_TAG_DATA':
			if tag == 'b':
				self.state = 'DESCRIPTION'
			self.spell.description += '</' + tag + '>'
		else:
			if tag == 'i':
				self.state = ''
			elif self.isCastingTime(tag):
				self.bCounter += 1
				self.state = 'CASTING'    
			elif self.isRange(tag):
				self.bCounter += 1
				self.state = 'RANGE'    
			elif self.isComponents(tag):
				self.bCounter += 1
				self.state = 'COMPONENTS'    
			elif self.isDuration(tag):
				self.bCounter += 1
				self.state = 'DURATION'

	def handle_data(self, data):
		# print("Encountered some data  :", data)
		# print('State:' + self.state)
		if self.state == 'TYPE_READING':
			self.state = ''
			self.spell.type = data
		elif self.state == 'CASTING':
			self.state = ''
			self.spell.castingTime = data.strip()
		elif self.state == 'RANGE':
			self.state = ''
			self.spell.range = data.strip()
		elif self.state == 'COMPONENTS':
			self.state = ''
			self.spell.components = data.strip()
		elif self.state == 'DURATION':
			self.state = ''
			self.spell.duration = data.strip()
		elif self.state == 'DESCRIPTION':
			# print("Encountered some data  :", data)
			self.spell.description += data.replace('•', '<br/>•')
		elif self.state == 'DESCRIPTION_B_TAG_DATA':
			# print("Encountered some data  :", data)
			self.spell.description += data


	def __init__(self, spell):
		super().__init__()
		self.reset()
		self.fed = []
		self.spell = spell
		self.state = ''
		self.bCounter = 0



files = [f for f in os.listdir('.') if os.path.isfile(f)]
for filename in files:
	if '.html' in filename:
		print(filename)
		with open(filename, encoding='utf8') as f:
			spell = Spell(filename.split('.')[0])
			parser = SpellHTMLParser(spell)
			parser.feed(f.read())
			spells.append(parser.spell)

with codecs.open('spells.json', 'w', 'utf8') as f:
	f.write('[\n')	
	delimiter = ''
	for spell in spells:
		f.write(delimiter)	
		f.write(spell.toJson())
		delimiter = ', '
	f.write('\n]')