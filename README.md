# Bozar
A Java bytecode obfuscator with GUI

![alt text](https://i.imgur.com/SmgJbll.png)

## Usage
* Download the version you want in [releases](https://github.com/vimasig/Bozar/releases) for your platform
* Run the executable.
* Done.

Let me know if obfuscator fails. Submit an [issue](https://github.com/vimasig/Bozar/issues) here.

## Currently Available Options
* Watermark
* Renamer
* Shuffler
* Decompiler crasher
* Control Flow obfuscation
* Constant obfuscation <sub><sup>(String literals and numbers)</sup></sub>
* Line number obfuscation
* Local variable obfuscation
* Inner class remover
* Source file/debug remover  

## Building
Some older maven versions have issues compiling this project.\
In such a case, use the latest version of maven to fix.
```
git clone https://github.com/vimasig/Bozar
cd Bozar
mvn compile javafx:run 
```

## Command Line Arguments
| Command | Description |
| --- | --- |
| `-input` | Target file path to obfuscate. |
| `-output` | Output path. |
| `-config` | Configuration path. |
| `-noupdate` | Disable update warnings. |
| `-console` | Application will run without GUI and obfuscation task will start immediately. |