---
title: Maven
keywords: deployment
tags: [deployment]
sidebar: overview_sidebar
permalink: ccri_maven.html
summary: "Maven How To"
---

## 1. Install Maven ##

TODO

## 2. Maven Artifacts ##

Artifacts are stored under kevinmayfield's account on https://bintray.com/kevinmayfield/careconnect
Projects are uploaded.

```
mvn deploy
```

These can't be SNAPSHOT artifacts, needs mvn security configured in settings.xml. (this is found in /Users/kevinmayfield/.m2/ on mac's)

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>

    <profiles>
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-kevinmayfield-careconnect</id>
                    <name>bintray</name>
                    <url>http://dl.bintray.com/kevinmayfield/careconnect</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-kevinmayfield-careconnect</id>
                    <name>bintray-plugins</name>
                    <url>http://dl.bintray.com/kevinmayfield/careconnect</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
    <servers>
      <server>
        <id>bintray-kevinmayfield-careconnect</id>
        <username>kevinmayfield</username>
        <password>----removed------</password>
      </server>
    </servers>
</settings>
```
