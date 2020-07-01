想获取该项目的更多解释，请前往原仓库。

参考自：https://github.com/stardust95/TinyCompiler

### 运行环境：

- Ununtu LTS 18.04(64位)
- LLVM版本：3.9
- Flex版本：2.6.4
- Bison版本：3.0.4

### 使用说明

- 编译TinyCompiler

  `make`

由于和源项目ubuntu版本不同，接下来执行方法和源项目略有不同

- 使用TinyCompiler编译test.input文件，将目标代码输出到output.o

  这一步按照原项目是执行不了的，可以直接把要测试的代码放到`test.input`文件里，然后对该文件执行下面命令：

  `cat test.input | ./compiler`

- 用g++链接output.o生成可执行文件

  具体原因可参考这里：https://blog.csdn.net/weixin_43207025/article/details/106815625

  `g++ -fno-pie -no-pie output.o -o test`

- 运行生成文件，即可看到输出

  `./test`