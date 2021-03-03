# Bozar
A Java bytecode obfuscator with GUI

![alt text](https://i.imgur.com/c3fK6tN.png)

## Usage
* Download the version you want in [releases](https://github.com/vimasig/Bozar/releases) for your platform
* Extract contents from the ZIP. <sub><sup>(That's not required by most ZIP tools)</sup></sub>
* Run the executable.
* Done.

Let me know if obfuscator fails. Submit an [issue](https://github.com/vimasig/Bozar/issues) here.

## Currently Available Options
* Watermark
* Renamer
* Decompiler crasher
* Control Flow obfuscation
* Constant obfuscation <sub><sup>(String literals and numbers)</sup></sub>
* Line number obfuscation
* Local variable obfuscation
* Source file/debug remover  

## Command Line Arguments
| Command | Description |
| --- | --- |
| `-input` | Target file path to obfuscate. |
| `-output` | Output path. |
| `-config` | Configuration path. |
| `-console` | Application will run without GUI and obfuscation task will start immediately. |