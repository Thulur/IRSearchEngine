#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import re
import urllib

from urllib.request import urlopen

url = "http://www.google.com/googlebooks/uspto-patents-grants-text.html"
page_content = urlopen(url).read().decode("utf-8")

links = re.findall('http://storage.googleapis.com/patents/grant_full_text/20\d{2}/ipg\d{6}.zip', page_content)
file_count = 0
for link in links:
    f = urlopen(link)

    with open("../data/ipgzip/" + link[-13:], "wb") as local_file:
        local_file.write(f.read())
        file_count += 1
        print("Downloaded " + str(file_count) + "/" + str(len(links)))
        
