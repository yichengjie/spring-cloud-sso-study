1. 安装maven插件
    ```xml
    <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>1.8.0</version>
        <configuration>
            <nodeVersion>v10.16.3</nodeVersion>
        </configuration>
        <executions>
            <execution>
                <id>install-npm</id>
                <goals>
                    <goal>install-node-and-npm</goal>
                </goals>
            </execution>
            <execution>
                <id>npm-install</id>
                <goals>
                    <goal>npm</goal>
                </goals>
            </execution>
            <execution>
                <id>npm-build</id>
                <goals>
                    <goal>npm</goal>
                </goals>
                <configuration>
                    <arguments>run-script build</arguments>
                </configuration>
            </execution>
        </executions>
    </plugin>
    ```
2. 项目根目录新建npm文件
    ```txt
    #!/bin/sh
    cd $(dirname $0)
    PATH="$PWD/node/":$PATH
    node "node/node_modules/npm/bin/npm-cli.js" "$@"
    ```
3. 项目根目录新建ng文件
    ```txt
    #!/bin/sh
    cd $(dirname $0)
    PATH="$PWD/node/":"$PWD":$PATH
    node_modules/@angular/cli/bin/ng "$@"
    ```
4. git bash进入项目根目录执行命令
    ```txt
    4.1 安装ng命令行工具: ./npm install @angular/cli@8.0.3
    4.2 ng新建一个hello项目: ./ng new hello
    4.3 安装bootstrap和jquery: ./npm install bootstrap@3 jquery --save
    4.3 将hello目录中的文件全部移到项目根目录中
    ```
5. 修改angular.json中outputPath的输出路径为src/main/resources/static
6. 执行命令编译源码
    ```txt
    ./ng build --watch
    ```
7. 启动springboot项目

