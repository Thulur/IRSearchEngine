#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import zipfile


file_count = 0
doc_collector_file = open("../data/xmlfiles.txt", "w")
for filename in os.listdir("../data/ipgzip"):
    if filename.endswith(".zip"):
        fh = open("../data/ipgzip/" + filename, "rb")
        z = zipfile.ZipFile(fh)
        
        for name in z.namelist():
            if not os.path.isfile("../data/ipgxml/" + name):
                outpath = "../data/ipgxml"
                z.extract(name, outpath)
                
                fhr = open("../data/ipgxml/" + name, "r")
                fhw = open("../data/ipgxml/" + name + ".tmp", "w")

                fhw.write('<?xml version="1.0" encoding="UTF-8"?>\n')
                fhw.write('<!DOCTYPE us-patent-grant SYSTEM "us-patent-grant-v45-2014-04-03.dtd" [ ]>\n')
                fhw.write("<myroot>\n")

                for line in fhr:
                    if line.startswith("<?xml") or line.startswith("<!DOCTYPE"):
                        continue
                    fhw.write(line)
            
                fhw.write("</myroot>\n")

                fhr.close()
                fhw.close()
                os.remove("../data/ipgxml/" + name)
                os.rename("../data/ipgxml/" + name + ".tmp", "../data/ipgxml/" + name)

            if file_count == 0:
                doc_collector_file.write(name)
            else:
                doc_collector_file.write("," + name)
            file_count += 1
            print("Processed " + str(file_count) + " files")

        fh.close()
            
