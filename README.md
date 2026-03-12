# BookletImposer V1.0.0

Tool for converting A4 PDFs into A5 booklet printing layout for simlex and duplex printers.

## Download 

Download the latest `.jar` file from:
[RELEASES](https://github.com/majchjan/BookletImposer/releases)

## Usage
```
java -jar -i <inputFile> [-o outputFile] [--isDuplexPrinter]
```

### Options
| Option | Description |
|--------|-------------|
| `-i --input <file>` | Input PDF [required] |
| `-o --output <file>` | Name of output file [optional] (if not used output file: 'inputfile + "_booklet.pdf"'|
| `--isDuplexPrinter` | Sets layout for Duplex Printer (File can be printed just by click print. To print booklet from layout for Simplex Printer you have to print first half of pages, then reverse printed sheets and print another half of pages.) |

## Build

To build the Maven project yourself, needed to use the following command in the project folder:
```
mvn package
```
JAR file appears in `target/`

## Exaple

If you have a PDF like:
| | | | | |
| - | - | - | - | - |
| ![Page1](./img/input/input1.png) | ![Page2](./img/input/input2.png) | ![Page3](./img/input/input3.png) | ![Page4](./img/input/input4.png) | ![Page5](./img/input/input5.png) |
| ![Page6](./img/input/input6.png) | ![Page7](./img/input/input7.png) | ![Page8](./img/input/input8.png) | ![Page9](./img/input/input9.png) | ![Page10](./img/input/input10.png) |

and use following command:

```
java -jar bookletimposer.jar -i document.pdf -o booklet.pdf
```

you get *booklet.pdf*:

| | | |
| - | - | - |
| ![Page1](./img/outputSimplex/output1_1.png) | ![Page2](./img/outputSimplex/output1_2.png) | ![Page3](./img/outputSimplex/output1_3.png) |
| ![Page4](./img/outputSimplex/output1_4.png) | ![Page5](./img/outputSimplex/output1_5.png) | ![Page6](./img/outputSimplex/output1_6.png) |

then you have to print 1-3 pages, reverse sheets and print 4-6 pages.

---

When you use following command:
```
java -jar bookletimposer.jar -i document.pdf -o booklet.pdf --isDuplexPrinter
```
you get *booklet.pdf*:

| | | |
| - | - | - |
| ![Page1](./img/outputDuplex/output2_1.png) | ![Page2](./img/outputDuplex/output2_2.png) | ![Page3](./img/outputDuplex/output2_3.png) |
| ![Page4](./img/outputDuplex/output2_4.png) | ![Page5](./img/outputDuplex/output2_5.png) | ![Page6](./img/outputDuplex/output2_6.png) |

and just print with duplex printer.