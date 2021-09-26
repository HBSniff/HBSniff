# HBSniff
HBSniff (**H**i**B**ernate **Sniff**er) is a static analysis tool for Java Hibernate ORM (Object-Relational Mapping) code smell detection.     

Latest Release: https://github.com/HBSniff/HBSniff/releases/tag/v1.6.7        
Documentations: https://HBSniff.github.io/     

# Highlights
* No Project Compilation Needed ([JavaParser-based](https://javaparser.org/))
* 14 Detectable Smells
* 4 Mapping Metrics
* Excel Report Visualization

# Usage
1. Setup JDK     
Install [Java Development Kit](https://www.oracle.com/java/technologies/downloads/) version greater than 8.0. Skip if you already have one.     
2. Download JAR     
Download [the latest release](https://github.com/HBSniff/HBSniff/releases/tag/v1.6.7) (HBSniff-1.6.7-jar-with-dependencies.jar) to the directory ```$downloadPath$```.    
3. Open Terminal     
Open cmd (Windows) or terminal (macOS, Linux).   
4. Execute Command with ```--input``` and ```--output``` path specified  
```bash
cd $downloadPath$
java -jar HBSniff-1.6.7-jar-with-dependencies.jar -i $projectRootPath$ -o $outputPath$
```

# Cite this Tool 
Zijie Huang, Zhiqing Shao, Guisheng Fan, Huiqun Yu, Kang Yang, Ziyi Zhou. HBSniff: A Static Analysis Tool for Java Hibernate Object-Relational Mapping Code Smell Detection. Science of Computer Programming. Under Review. https://hbsniff.github.io/paper.pdf