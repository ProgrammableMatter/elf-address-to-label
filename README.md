elf mapper
===========
Replaces address occurrences matching **0x[0-9a-zA-Z]{4}**  by labels as shown by **readelf -a**. This usefull when generating a .dot graph where addresses should be replaced by labels. In case of multiple labels, the address is replaced by a list: **{labe1, label2, labeln}**.

### Usage
    java -jar <mapper> <labelsFile> <fileToReplaceAddresses>
Writes the translated file to **pretty-fileToReplaceAddresses**.
    
### Compile

    mvn clean assembly:single
    
### Run

    readelf -a > elfDump.txt
    java -jar ./target/elf-mapper-0.0.2-SNAPSHOT-jar-with-dependencies.jar elfDump.txt fileToReplaceAddresses.txt

### Example
A .dot file snippet with replaced addresses.

| input                                    | mapped output                                                             |
|------------------------------------------|----------------------------------------------------------------------------
| ```digraph G {```                            |``` digraph g {``` |
| ```  "0x0000 - \n0x0004" [shape=box] ```     |```  "{__tmp_reg__, __vectors, __heap_end} - \n0x0004" [shape=box]``` |
| ```  "0x0120 - \n0x0122" [shape=hexagon]```  |```  "0x0120 - \nbufferbitpointerstart" [shape=hexagon]``` |
| ```  "0x0122 - \n0x0130" [shape=hexagon]```  |```  "bufferbitpointerstart - \nisdataendposition" [shape=hexagon]``` |
| ```  "0x0342 - \n0x0356" [shape=hexagon]```  |```  "setinitiatorstatestart - \nconstructcommunicationpro" [shape=hexagon]``` |
| ```  "0x0356 - \n0x0366" [shape=hexagon]```  |```  "constructcommunicationpro - \nconstructcommunicationpro" [shape=hexagon]``` |
| ```  "0x0606 - \n0x062C" [shape=ellipse]```  |```  "manchesterdecodebuffer - \n0x062c" [shape=ellipse]``` |
| ```  "0x0922 - \n0x0928" [shape=ellipse]```  |```  "receivesouth - \n0x0928" [shape=ellipse]``` |
| ```}```  |```}``` |
