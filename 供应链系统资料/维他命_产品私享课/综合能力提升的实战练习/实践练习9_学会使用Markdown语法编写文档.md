## 什么是Markdown？

Markdown是一种轻量级标记语言，排版语法简洁，让人们更多地关注内容本身而非排版。它使用易读易写的纯文本格式编写文档，可与HTML混编，可导出 HTML、PDF 以及本身的 .md 格式的文件。因简洁、高效、易读、易写，Markdown被大量使用，如Github、Wikipedia、简书、语雀、飞书、TAPD等工具上都可以只用Markdown。。

Markdown的语法十分简单，常用的标记符号不超过十个，用于日常写作记录绰绰有余，不到半小时就能完全掌握。

就是这十个不到的标记符号，却能让人**优雅地沉浸式记录，专注内容而不是纠结排版**，达到「心中无尘，码字入神」的境界。

## 为什么要使用 Markdown？

早期的时候，大家在网页上编写内容，常用的就是类似于Word的“富文本编辑器”，这一类编辑器在你编辑内容的时候还需要点击上方的工具来设置格式，这样的操作很容易打算写作的思路。

而且编辑器中的样式选项特别多，会让很多人写出来的文档千奇百怪，例如说字体，字体大小，字体颜色，字体背景色，行间距，段间距等，这些自由度越高，越容易让创作者的注意力被带偏。

推荐学习使用 Markdown 而不是 Word 类编辑器的原因，如下：

-   Markdown 无处不在。StackOverflow、CSDN、掘金、简书、GitBook、有道云笔记、V2EX、光谷社区等。主流的代码托管平台，如 GitHub、GitLab、BitBucket、Coding、Gitee 等等，都支持 Markdown 语法，很多开源项目的 README、开发文档、帮助文档、Wiki 等都用 Markdown 写作。
-   Markdown 是纯文本可移植的。几乎可以使用任何应用程序打开包含 Markdown 格式的文本文件。如果你不喜欢当前使用的 Markdown 应用程序了，则可以将 Markdown 文件导入另一个 Markdown 应用程序中。这与 Microsoft Word 等文字处理应用程序形成了鲜明的对比，Microsoft Word 将你的内容锁定在专有文件格式中。
-   Markdown 是独立于平台的。你可以在运行任何操作系统的任何设备上创建 Markdown 格式的文本。
-   Markdown 能适应未来的变化。即使你正在使用的应用程序将来会在某个时候不能使用了，你仍然可以使用文本编辑器读取 Markdown 格式的文本。当涉及需要无限期保存的书籍、大学论文和其他里程碑式的文件时，这是一个重要的考虑因素。

> 作为产品经理来说，日常输出的文档可能会在不同的编辑器中，可能是在线的，可能是本地的。这些编辑器的内容如果格式不一样，那么从A复制到B的时候，就会导致还需要再调整一次样式结构，这样会大大增加自己的工作量。

## Markdown语法

Markdown的语法并不多，而且高频使用的就只有几个，只需要不到20分钟的时间就可以掌握它，习惯了之后可以大大地提升自己文档编辑的速度。

而且目前有很多在线编辑器（语雀、飞书、TAPD、有道云笔记等），已经支持了**Markdown和富文本内容混排**，也就是有一些样式可以通过Markdown的语法来设置，有一些也可以直接用顶部的工具栏来设置，两者可以达到一样的效果，大大地降低了入门上手的难度。

### 基本语法

| 元素 | Markdown 语法 |
| --- | --- |
| [标题（Heading）](https://markdown.com.cn/basic-syntax/headings.html) | # H1<br>## H2<br>### H3 |
| [粗体（Bold）](https://markdown.com.cn/basic-syntax/bold.html) | **bold text** |
| [斜体（Italic）](https://markdown.com.cn/basic-syntax/italic.html) | *italicized text* |
| [引用块（Blockquote）](https://markdown.com.cn/basic-syntax/blockquotes.html) | > blockquote |
| [有序列表（Ordered List）](https://markdown.com.cn/basic-syntax/ordered-lists.html) | 1. First item<br>2. Second item<br>3. Third item |
| [无序列表（Unordered List）](https://markdown.com.cn/basic-syntax/unordered-lists.html) | - First item<br>- Second item<br>- Third item |
| [代码（Code）](https://markdown.com.cn/basic-syntax/code.html) | `code` |
| [分隔线（Horizontal Rule）](https://markdown.com.cn/basic-syntax/horizontal-rules.html) | --- |
| [链接（Link）](https://markdown.com.cn/basic-syntax/links.html) | [title](https://www.example.com) |
| [图片（Image）](https://markdown.com.cn/basic-syntax/images.html) | ![alt text](image.jpg) |

### 扩展语法

这些元素通过添加额外的功能扩展了基本语法。但是，并非所有 Markdown 应用程序都支持这些元素。

| 元素 | Markdown 语法 |
| --- | --- |
| [表格（Table）](https://markdown.com.cn/extended-syntax/tables.html) | \| Syntax \| Description \|<br>\| ----------- \| ----------- \|<br>\| Header \| Title \|<br>\| Paragraph \| Text \| |
| [代码块（Fenced Code Block）](https://markdown.com.cn/extended-syntax/fenced-code-blocks.html) | ```<br>{<br>"firstName": "John",<br>"lastName": "Smith",<br>"age": 25<br>}<br>``` |
| [脚注（Footnote）](https://markdown.com.cn/extended-syntax/footnotes.html) | Here's a sentence with a footnote. [^1]<br>[^1]: This is the footnote. |
| [标题编号（Heading ID）](https://markdown.com.cn/extended-syntax/heading-ids.html) | ### My Great Heading {#custom-id} |
| [定义列表（Definition List）](https://markdown.com.cn/extended-syntax/definition-lists.html) | term<br>: definition |
| [删除线（Strikethrough）](https://markdown.com.cn/extended-syntax/strikethrough.html) | ~~The world is flat.~~ |
| [任务列表（Task List）](https://markdown.com.cn/extended-syntax/task-lists.html) | - [x] Write the press release<br>- [ ] Update the website<br>- [ ] Contact the media |

## ​