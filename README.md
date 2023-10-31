# Bozar - Java Bytecode Obfuscator with GUI

![alt text](https://i.imgur.com/SmgJbll.png)

## :tada: Exciting News! Bozar 2.0.0 Coming Soon! :tada:

We are delighted to announce that Bozar version 2.0.0 is on the horizon, bringing important enhancements to our Java bytecode obfuscation tool:

**Key Features of Bozar 2.0.0:**

- :rocket: **Simplified User Experience:** Bozar 2.0.0 is incredibly lightweight and user-friendly. No complex configurations are required. Whether you're a seasoned developer or a newcomer, you can start obfuscating your applications effortlessly.

- :lock: **Advanced Obfuscation Techniques:** Bozar 2.0.0 introduces new, robust obfuscation methods that bolster the security of your code. Your applications will benefit from enhanced protection.

- :bulb: **Community-Driven Improvements:** We've listened to the valuable feedback and insights from our GitHub community to make Bozar even better. Your input has been instrumental in shaping this release.

Stay tuned for Bozar 2.0.0! We look forward to sharing these significant enhancements with you.

To stay updated and participate in discussions, consider joining our [Discord Community](https://discord.gg/Yp3sDQ7y6S) – a hub for sharing knowledge and support as we approach the release of Bozar 2.0.0.

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
