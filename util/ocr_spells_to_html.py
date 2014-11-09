__author__ = 'Servy'

import re

with open("FullSpellList.txt") as f:
    lines = f.readlines()

def normalize_name(s):
    res = []
    buf = ""
    for c in str(s):
        if c.isupper():
            res.append(buf)
            buf = str(c)
        elif c != ' ':
            buf += c
    res.append(buf)
    buf = str(c)
    return " ".join(res).replace("/", "-").replace("\\", "-")

def normalize_desc_str(s):
    s = s.replace("o f", "of")
    return re.sub("[0-9]+d[0-9]+[ +-]*[0-9]*", lambda m: "<b>" + m.group(0) + "</b>", s)


def format_spell(lines):
    name = normalize_name(lines[0].strip())
    type = lines[1].strip()
    props = []
    desc = []
    propsDone = False
    for line in lines[2:]:
        if propsDone:
            desc.append(line.strip())
        else:
            if ":" in line:
                props.append(line.strip())
            else:
                props[len(props) - 1] = props[len(props) - 1] + " " + line.strip()

            if line.strip().startswith("Duration:"):
                propsDone = True

    result = """<html>
    <head>
        <link href="style.css" rel="stylesheet" />
    </head>
    <body>
        <header>
        <i>%s</i><br/>
""" % type.strip()

    for p in props:
        propInfo, propDesc = p.split(":", 1)
        result += " " * 8 + ("<b>%s:</b> %s<br/>\n" % (propInfo, propDesc))

    result += """
        </header>
        <article>
            <p>
"""
    for line in desc:
        result += " " * 12 + normalize_desc_str(line.strip()) + "\n"
        if line.strip().endswith("."):
            result += """
            </p>
            <p>
"""

    result += """
            </p>
        </article>
    </body>
</html>"""
    return (name, result)


def doSpell(spell):
    if len(spell) > 0:
        name, html = format_spell(spell)
        with open("%s.html" % (name), "w") as f:
            f.write(html)


buffer = []
for line in lines:
    buffer.append(line)
    if line.strip().startswith("Casting Time:"):
        spell = buffer[0:-3]
        if len(spell) > 0:
            doSpell(spell)
            buffer = buffer[-3:]

doSpell(buffer)