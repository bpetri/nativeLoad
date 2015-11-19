#/!/usr/bin/env

import httplib
import json
import os
import xml.etree.ElementTree as ET
import sys
import zipfile

######################################
### Functions                      ###
######################################

def deleteEntitiesInAce(connection, workspaceUrl, entityName) :
    #print "Deleting %s entries at %s/%s" % (entityName, workspaceUrl, entityName)
    connection.request("GET", "%s/%s" % (workspaceUrl, entityName))
    response = connection.getresponse()
    raw = response.read()
    entities = json.loads(raw)
    for entity in entities :
        connection.request("DELETE", "%s/%s/%s" % (workspaceUrl, entityName, entity))
        response = connection.getresponse()
        response.read()
        if response.status != 200 :
            print "error deleting %s entry. response status is %d" % (entityName, response.status)

def createAceEntityEntry(connection, workspaceUrl, entityName, attributes, tags={}) :
    conn.request("POST", "%s/%s" % (workspaceUrl, entityName), json.dumps( { "attributes" : attributes, "tags" : tags } ))
    response = conn.getresponse()
    if response.status != 302 :
        print "Error creating %s entry with attributes %s. http response is %d" % (entityName, attributes, response.status)
        print response.read()
    response.read()

######################################
### Main script                    ###
######################################


#----------Check arguments--------------
if len(sys.argv) < 3 :
        print "usage is: %s config_xml_path bundle_dir\n" % sys.argv[0]
        exit(1)


#connect to ace and create workspace
conn = httplib.HTTPConnection("localhost:8080")
conn.request("POST", "/client/work")
print conn
response = conn.getresponse()
#print response.status
#note should be 302 with location to new workspace /client/work/WORK_ID
headers = response.getheaders()
loc = response.getheader("location")

print "Location of workspace %s" % (loc)
response.read()


#Delete old entry
deleteEntitiesInAce(conn, loc, "artifact")
deleteEntitiesInAce(conn, loc, "artifact2feature")
deleteEntitiesInAce(conn, loc, "feature")
deleteEntitiesInAce(conn, loc, "feature2distribution")
deleteEntitiesInAce(conn, loc, "distribution")
deleteEntitiesInAce(conn, loc, "distribution2target")
deleteEntitiesInAce(conn, loc, "target")



#-----------------------------------
#
#    Create artifacts from bundles
#
#-----------------------------------
bundledir = sys.argv[2]

for (root, dirs, files) in os.walk(bundledir) :
    for filename in files :
        if not filename.endswith(".jar") :
            continue

        #----------Get information from .jar --------------
        filelocation = "%s/%s" % (root, filename)
        zipp = zipfile.ZipFile(filelocation)
        bundleSymbolicName = ""
        bundleVersion = ""
        bundleName = ""
        artifactName = ""
        uri = ""
        with zipp.open("META-INF/MANIFEST.MF") as mani:
            lineread = False
            while 1:
                if not lineread:
                    line = mani.readline()
                lineread = False
                if not line:
                    break

                elif line.startswith("Bundle-SymbolicName: "):
                    bundleSymbolicName = line.strip("Bundle-SymbolicName:").strip()
                    line = mani.readline()
                    lineread = True
                    if not line:
                        break
                    elif line.startswith(" "):
                        bundleSymbolicName = "%s%s" % (bundleSymbolicName, line.strip())

                elif line.startswith("Bundle-Version: "):
                    bundleVersion = line.strip("Bundle-Version:").strip()

                elif line.startswith("Bundle-Name: ") :
                    bundleName = line.strip("Bundle-Name:").strip()


        artifactName = "%s-%s" % (bundleName, bundleVersion)

        #----------Send .jar to OBR

        body = open(filelocation, "r")
        header = {"Content-type":"application/vnd.osgi.bundle"}

        conn.request("POST", "/obr", body, header)
        response = conn.getresponse()
        response.read()

        #-----------Get repository.xml to get uri
        conn.request("GET", "/obr/repository.xml")
        response = conn.getresponse()
        repository = ET.fromstring(response.read())

        for resource in repository:
            if resource.attrib["symbolicname"] == bundleSymbolicName :
                uri = "http://localhost:8080/obr/%s" % resource.attrib["uri"]

        #----------OBR -> Artifact------------
        attr = {
            "url" : uri,
            "artifactName" : artifactName,
            "mimetype" : "application/vnd.osgi.bundle" ,
            "Bundle-Name" : bundleName ,
            "Bundle-SymbolicName" : bundleSymbolicName ,
            "Bundle-Version" : bundleVersion ,
            "artifactDescription" : "" ,
            "processorPid" : ""
        }

        createAceEntityEntry(conn, loc, "artifact", attr)

#-----------------------------------
#
#    Parse config.xml
#
#-----------------------------------

tree = ET.parse(sys.argv[1])
config = tree.getroot()

#-----------------------------------
#
#    Create features and artifact2features
#
#-----------------------------------

#Go through all features
featureArchs = config.iter("features")
for featureArch in featureArchs:
   #get architecture
   cpu_arch = featureArch.attrib["arch"]

   features = featureArch.iter("feature")
   for feature in features:
      #Create feature
      featureDesc = feature.attrib["name"]
      featureName = "%s_%s" % (feature.attrib["name"], cpu_arch)
      featureAttr = { "name" : featureName, "description" : featureDesc }
      featureTags = {"arch": cpu_arch}
      createAceEntityEntry(conn, loc, "feature", featureAttr, featureTags)

      artifacts = feature.iter("artifact")
      for artifact in artifacts:
         #Create artifact2feature
         a2fAttr = { "leftEndpoint" : "(Bundle-SymbolicName=%s_%s)" % (artifact.attrib["name"], cpu_arch), "rightEndpoint" : "(name=%s)" % featureName }
         createAceEntityEntry(conn, loc, "artifact2feature", a2fAttr)

#-----------------------------------
#
#    Create distributions and feature2distributions
#
#-----------------------------------

#Go through all distributions
distributionArchs = config.iter("distributions")
for distributionArch in distributionArchs:
   #get architecture
   cpu_arch = distributionArch.attrib["arch"]

   #Create distribution
   distributions = distributionArch.iter("distribution")
   for distribution in distributions:
      distributionDesc = distribution.attrib["name"]
      distributionName = "%s_%s" % (distribution.attrib["name"], cpu_arch)
      distributionAttr = { "name" : distributionName, "description" : distributionDesc }
      distributionTags = { "arch" : cpu_arch }
      createAceEntityEntry(conn, loc, "distribution", distributionAttr, distributionTags)

      features = distribution.iter("feature")
      for feature in features:
         #Create feature2distribution
         featureName = "%s_%s" % (feature.attrib["name"], cpu_arch)
         f2dAttr = { "leftEndpoint" : "(name=%s)" % featureName, "rightEndpoint" : "(name=%s)" % distributionName }
         createAceEntityEntry(conn, loc, "feature2distribution", f2dAttr)


#-----------------------------------
#
#    Create distribution2targets
#
#-----------------------------------
#Go through all targets
targetArchs = config.iter("targets")
for targetArch in targetArchs:
   #get architecture
   cpu_arch = targetArch.attrib["arch"]

   #Get target
   targets = targetArch.iter("target")
   for target in targets:

      distributions = target.iter("distribution")
      for distribution in distributions:
         #Create distribution2target
         distributionName = "%s_%s" % (distribution.attrib["name"], cpu_arch)
         d2tAttr = { "leftCardinality" : 1, "leftEndpoint" : "(name=%s)" % distributionName, "rightCardinality" : 1000, "rightEndpoint" : "(&(target.role=%s)(target.cpu_arch=%s))" % (target.attrib["role"], cpu_arch)}
         createAceEntityEntry(conn, loc, "distribution2target", d2tAttr)


#activate workspace
conn.request("POST", loc)
response = conn.getresponse()
if response.status == 200 :
    print "Workspace %s activated" % loc
else :
    print "Error activating workspace"
response.read()
