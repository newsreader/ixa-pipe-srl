ixa-pipe-srl
============

This module is part of IXA-Pipeline, a multilingual NLP pipeline developed by the IXA NLP Group (ixa.si.ehu.es).

Ixa-pipe-srl provides a wrapper for English and Spanish dependency parser and semantic role labeller using mate-tools (https://code.google.com/p/mate-tools/). The module takes tokenized and POS-tagged text in NAF format as standard input and outputs syntactic and semantic analysis also in NAF.

The models for English and Spanish have been trained using PropBank(http://verbs.colorado.edu/~mpalmer/projects/ace.html), NomBank(http://nlp.cs.nyu.edu/meyers/NomBank.html) Ancora corpus (http://clic.ub.edu/corpus/en/ancora) in CoNLL 2009 Shared Task format (http://ufal.mff.cuni.cz/conll2009-st/).

The semantic annotation provided by the module is enriched using the PredicateMatrix (http://adimen.si.ehu.es/web/PredicateMatrix).


# INSTALLATION

Installing the ixa-pipe-srl requires the following steps:

If you already have installed in your machine JDK7 and MAVEN 3, please go to step 3 directly. Otherwise, follow these steps:

## 1. Install JDK 1.7

If you do not install JDK in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

    export JAVA_HOME=/yourpath/local/java7
    export PATH=${JAVA_HOME}/bin:${PATH}

If you use tcsh you will need to specify it in your .login as follows:

    setenv JAVA_HOME /usr/java/java17
    setenv PATH ${JAVA_HOME}/bin:${PATH}

If you re-login into your shell and run the command

    java -version

You should now see that your jdk is 1.7
    

## 2. Install MAVEN 3

Download MAVEN 3 from

    wget http://www.apache.org/dyn/closer.cgi/maven/maven-3/3.0.4/binaries/apache-maven-3.0.4-bin.tar.gz

Now you need to configure the PATH. For Bash Shell:

    export MAVEN_HOME=/path-to-apache-maven/apache-maven-3.0.4
    export PATH=${MAVEN_HOME}/bin:${PATH}

For tcsh shell:

    setenv MAVEN3_HOME ~/local/apache-maven-3.0.4
    setenv PATH ${MAVEN3}/bin:{PATH}

If you re-login into your shell and run the command

    mvn -version

You should see reference to the MAVEN version you have just installed plus the JDK that is using.

## 3. Get module source code

    git clone https://github.com/newsreader/ixa-pipe-srl
    
## 4. Get external references

Some dependencies are not included in maven repositories, but they can be found in the Mate-tools package. First download mate-tools package:

    wget https://mate-tools.googlecode.com/files/srl-4.3.tgz

Notice that the version of mate-tools needed is the 4.3. The module will not work with a higher version. The external references you should extract are seg.jar and srl.jar:

    tar -zxvf srl-4.3.tgz srl-20130917/lib/seg.jar
    tar -zxvf srl-4.3.tgz srl-20130917/srl.jar
    
Now, install these dependencies into the local maven repository:

    mvn install:install-file -Dfile=srl-20130917/srl.jar -DgroupId=local -DartifactId=srl -Dversion=1.0 -Dpackaging=jar
    mvn install:install-file -Dfile=srl-20130917/lib/seg.jar -DgroupId=local -DartifactId=seg -Dversion=1.0 -Dpackaging=jar

## 5. Move into main directory

    cd ixa-pipe-srl/IXA-EHU-srl

## 6. Install module using maven

    mvn clean package

This step will create a directory called target/ which contains various directories and files. Most importantly, there you will find the module executable:

IXA-EHU-srl-1.0.jar

This executable contains every dependency the module needs, so it is completely portable as long as you have a JVM 1.7 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

    mvn clean install

## 7. Download models

In the directory where you have the executable IXA-EHU-srl-1.0.jar you must create a directory for trained modules. This directory should contain two subdirectories, one for the english modules and another for the spanish ones:

    cd /directory-with-jar
    mkdir models
    mkdir models/eng
    mkdir models/spa

The module needs models for dependency parsing and semantic role labeling. You can get all the models required with the following commands:

    wget http://mate-tools.googlecode.com/files/CoNLL2009-ST-English-ALL.anna-3.3.parser.model --directory-prefix=models/eng/
    wget http://fileadmin.cs.lth.se/nlp/models/srl/en/srl-20100906/srl-eng.model --directory-prefix=models/eng/


    wget http://mate-tools.googlecode.com/files/CoNLL2009-ST-Spanish-ALL.anna-3.3.parser.model --directory-prefix=models/spa/
    wget http://adimen.si.ehu.es/web/files/AnCoraModel/srl-spa.model --directory-prefix=models/spa/
    
    
## 8. Download the Predicate Matrix

In the directory where you have the executable IXA-EHU-srl-1.0.jar you must create a directory called PredicateMatrix.

    cd /directory-with-jar
    mkdir PredicateMatrix
    
Now download and unpack the PredicateMatrix into that directory:

    wget http://adimen.si.ehu.es/web/files/PredicateMatrix/PredicateMatrix.srl-module.tar.gz --directory-prefix=
    PredicateMatrix/
    tar -zxf PredicateMatrix/PredicateMatrix.srl-module.tar.gz
