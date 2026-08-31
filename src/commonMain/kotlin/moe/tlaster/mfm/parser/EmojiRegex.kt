package moe.tlaster.mfm.parser

/*
 * Emoji pattern derived from @misskey-dev/emoji-data 17.0.0.
 *
 * MIT License
 *
 * Copyright (c) Misskey Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
private val unicodeEmojiRegex by lazy { Regex(encodeEmojiPattern("""(?:\ud83d\udc68\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83e\uddd1\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83e\uddd1\ud83c[\udffc-\udfff]|\ud83e\uddd1\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83e\uddd1\ud83c[\udffb\udffd-\udfff]|\ud83e\uddd1\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83e\uddd1\ud83c[\udffb\udffc\udffe\udfff]|\ud83e\uddd1\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83e\uddd1\ud83c[\udffb-\udffd\udfff]|\ud83e\uddd1\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83e\uddd1\ud83c[\udffb-\udffe]|\ud83d\udc68\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udffb\u200d\ud83d\udc30\u200d\ud83d\udc68\ud83c[\udffc-\udfff]|\ud83d\udc68\ud83c\udffb\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffc-\udfff]|\ud83d\udc68\ud83c\udffb\u200d\ud83e\udeef\u200d\ud83d\udc68\ud83c[\udffc-\udfff]|\ud83d\udc68\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udffc\u200d\ud83d\udc30\u200d\ud83d\udc68\ud83c[\udffb\udffd-\udfff]|\ud83d\udc68\ud83c\udffc\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb\udffd-\udfff]|\ud83d\udc68\ud83c\udffc\u200d\ud83e\udeef\u200d\ud83d\udc68\ud83c[\udffb\udffd-\udfff]|\ud83d\udc68\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udffd\u200d\ud83d\udc30\u200d\ud83d\udc68\ud83c[\udffb\udffc\udffe\udfff]|\ud83d\udc68\ud83c\udffd\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb\udffc\udffe\udfff]|\ud83d\udc68\ud83c\udffd\u200d\ud83e\udeef\u200d\ud83d\udc68\ud83c[\udffb\udffc\udffe\udfff]|\ud83d\udc68\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udffe\u200d\ud83d\udc30\u200d\ud83d\udc68\ud83c[\udffb-\udffd\udfff]|\ud83d\udc68\ud83c\udffe\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb-\udffd\udfff]|\ud83d\udc68\ud83c\udffe\u200d\ud83e\udeef\u200d\ud83d\udc68\ud83c[\udffb-\udffd\udfff]|\ud83d\udc68\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc68\ud83c\udfff\u200d\ud83d\udc30\u200d\ud83d\udc68\ud83c[\udffb-\udffe]|\ud83d\udc68\ud83c\udfff\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb-\udffe]|\ud83d\udc68\ud83c\udfff\u200d\ud83e\udeef\u200d\ud83d\udc68\ud83c[\udffb-\udffe]|\ud83d\udc69\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffb\u200d\ud83d\udc30\u200d\ud83d\udc69\ud83c[\udffc-\udfff]|\ud83d\udc69\ud83c\udffb\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffc-\udfff]|\ud83d\udc69\ud83c\udffb\u200d\ud83e\udd1d\u200d\ud83d\udc69\ud83c[\udffc-\udfff]|\ud83d\udc69\ud83c\udffb\u200d\ud83e\udeef\u200d\ud83d\udc69\ud83c[\udffc-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\ud83d\udc30\u200d\ud83d\udc69\ud83c[\udffb\udffd-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb\udffd-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\ud83e\udd1d\u200d\ud83d\udc69\ud83c[\udffb\udffd-\udfff]|\ud83d\udc69\ud83c\udffc\u200d\ud83e\udeef\u200d\ud83d\udc69\ud83c[\udffb\udffd-\udfff]|\ud83d\udc69\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffd\u200d\ud83d\udc30\u200d\ud83d\udc69\ud83c[\udffb\udffc\udffe\udfff]|\ud83d\udc69\ud83c\udffd\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb\udffc\udffe\udfff]|\ud83d\udc69\ud83c\udffd\u200d\ud83e\udd1d\u200d\ud83d\udc69\ud83c[\udffb\udffc\udffe\udfff]|\ud83d\udc69\ud83c\udffd\u200d\ud83e\udeef\u200d\ud83d\udc69\ud83c[\udffb\udffc\udffe\udfff]|\ud83d\udc69\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udffe\u200d\ud83d\udc30\u200d\ud83d\udc69\ud83c[\udffb-\udffd\udfff]|\ud83d\udc69\ud83c\udffe\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb-\udffd\udfff]|\ud83d\udc69\ud83c\udffe\u200d\ud83e\udd1d\u200d\ud83d\udc69\ud83c[\udffb-\udffd\udfff]|\ud83d\udc69\ud83c\udffe\u200d\ud83e\udeef\u200d\ud83d\udc69\ud83c[\udffb-\udffd\udfff]|\ud83d\udc69\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83d\udc68\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83d\udc69\ud83c[\udffb-\udfff]|\ud83d\udc69\ud83c\udfff\u200d\ud83d\udc30\u200d\ud83d\udc69\ud83c[\udffb-\udffe]|\ud83d\udc69\ud83c\udfff\u200d\ud83e\udd1d\u200d\ud83d\udc68\ud83c[\udffb-\udffe]|\ud83d\udc69\ud83c\udfff\u200d\ud83e\udd1d\u200d\ud83d\udc69\ud83c[\udffb-\udffe]|\ud83d\udc69\ud83c\udfff\u200d\ud83e\udeef\u200d\ud83d\udc69\ud83c[\udffb-\udffe]|\ud83e\uddd1\ud83c\udffb\u200d\u2764\ufe0f\u200d\ud83e\uddd1\ud83c[\udffc-\udfff]|\ud83e\uddd1\ud83c\udffb\u200d\ud83d\udc30\u200d\ud83e\uddd1\ud83c[\udffc-\udfff]|\ud83e\uddd1\ud83c\udffb\u200d\ud83e\udd1d\u200d\ud83e\uddd1\ud83c[\udffb-\udfff]|\ud83e\uddd1\ud83c\udffb\u200d\ud83e\udeef\u200d\ud83e\uddd1\ud83c[\udffc-\udfff]|\ud83e\uddd1\ud83c\udffc\u200d\u2764\ufe0f\u200d\ud83e\uddd1\ud83c[\udffb\udffd-\udfff]|\ud83e\uddd1\ud83c\udffc\u200d\ud83d\udc30\u200d\ud83e\uddd1\ud83c[\udffb\udffd-\udfff]|\ud83e\uddd1\ud83c\udffc\u200d\ud83e\udd1d\u200d\ud83e\uddd1\ud83c[\udffb-\udfff]|\ud83e\uddd1\ud83c\udffc\u200d\ud83e\udeef\u200d\ud83e\uddd1\ud83c[\udffb\udffd-\udfff]|\ud83e\uddd1\ud83c\udffd\u200d\u2764\ufe0f\u200d\ud83e\uddd1\ud83c[\udffb\udffc\udffe\udfff]|\ud83e\uddd1\ud83c\udffd\u200d\ud83d\udc30\u200d\ud83e\uddd1\ud83c[\udffb\udffc\udffe\udfff]|\ud83e\uddd1\ud83c\udffd\u200d\ud83e\udd1d\u200d\ud83e\uddd1\ud83c[\udffb-\udfff]|\ud83e\uddd1\ud83c\udffd\u200d\ud83e\udeef\u200d\ud83e\uddd1\ud83c[\udffb\udffc\udffe\udfff]|\ud83e\uddd1\ud83c\udffe\u200d\u2764\ufe0f\u200d\ud83e\uddd1\ud83c[\udffb-\udffd\udfff]|\ud83e\uddd1\ud83c\udffe\u200d\ud83d\udc30\u200d\ud83e\uddd1\ud83c[\udffb-\udffd\udfff]|\ud83e\uddd1\ud83c\udffe\u200d\ud83e\udd1d\u200d\ud83e\uddd1\ud83c[\udffb-\udfff]|\ud83e\uddd1\ud83c\udffe\u200d\ud83e\udeef\u200d\ud83e\uddd1\ud83c[\udffb-\udffd\udfff]|\ud83e\uddd1\ud83c\udfff\u200d\u2764\ufe0f\u200d\ud83e\uddd1\ud83c[\udffb-\udffe]|\ud83e\uddd1\ud83c\udfff\u200d\ud83d\udc30\u200d\ud83e\uddd1\ud83c[\udffb-\udffe]|\ud83e\uddd1\ud83c\udfff\u200d\ud83e\udd1d\u200d\ud83e\uddd1\ud83c[\udffb-\udfff]|\ud83e\uddd1\ud83c\udfff\u200d\ud83e\udeef\u200d\ud83e\uddd1\ud83c[\udffb-\udffe]|\ud83d\udc68\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68|\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d[\udc68\udc69]|\ud83e\udef1\ud83c\udffb\u200d\ud83e\udef2\ud83c[\udffc-\udfff]|\ud83e\udef1\ud83c\udffc\u200d\ud83e\udef2\ud83c[\udffb\udffd-\udfff]|\ud83e\udef1\ud83c\udffd\u200d\ud83e\udef2\ud83c[\udffb\udffc\udffe\udfff]|\ud83e\udef1\ud83c\udffe\u200d\ud83e\udef2\ud83c[\udffb-\udffd\udfff]|\ud83e\udef1\ud83c\udfff\u200d\ud83e\udef2\ud83c[\udffb-\udffe]|\ud83d\udc68\u200d\u2764\ufe0f\u200d\ud83d\udc68|\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d[\udc68\udc69]|\ud83e\uddd1\u200d\ud83e\udd1d\u200d\ud83e\uddd1|\ud83d\udc6f\ud83c\udffb\u200d\u2640\ufe0f|\ud83d\udc6f\ud83c\udffb\u200d\u2642\ufe0f|\ud83d\udc6f\ud83c\udffc\u200d\u2640\ufe0f|\ud83d\udc6f\ud83c\udffc\u200d\u2642\ufe0f|\ud83d\udc6f\ud83c\udffd\u200d\u2640\ufe0f|\ud83d\udc6f\ud83c\udffd\u200d\u2642\ufe0f|\ud83d\udc6f\ud83c\udffe\u200d\u2640\ufe0f|\ud83d\udc6f\ud83c\udffe\u200d\u2642\ufe0f|\ud83d\udc6f\ud83c\udfff\u200d\u2640\ufe0f|\ud83d\udc6f\ud83c\udfff\u200d\u2642\ufe0f|\ud83e\udd3c\ud83c\udffb\u200d\u2640\ufe0f|\ud83e\udd3c\ud83c\udffb\u200d\u2642\ufe0f|\ud83e\udd3c\ud83c\udffc\u200d\u2640\ufe0f|\ud83e\udd3c\ud83c\udffc\u200d\u2642\ufe0f|\ud83e\udd3c\ud83c\udffd\u200d\u2640\ufe0f|\ud83e\udd3c\ud83c\udffd\u200d\u2642\ufe0f|\ud83e\udd3c\ud83c\udffe\u200d\u2640\ufe0f|\ud83e\udd3c\ud83c\udffe\u200d\u2642\ufe0f|\ud83e\udd3c\ud83c\udfff\u200d\u2640\ufe0f|\ud83e\udd3c\ud83c\udfff\u200d\u2642\ufe0f|\ud83d\udc6f\u200d\u2640\ufe0f|\ud83d\udc6f\u200d\u2642\ufe0f|\ud83e\udd3c\u200d\u2640\ufe0f|\ud83e\udd3c\u200d\u2642\ufe0f|\ud83d\udc6b\ud83c[\udffb-\udfff]|\ud83d\udc6c\ud83c[\udffb-\udfff]|\ud83d\udc6d\ud83c[\udffb-\udfff]|\ud83d\udc6f\ud83c[\udffb-\udfff]|\ud83d\udc8f\ud83c[\udffb-\udfff]|\ud83d\udc91\ud83c[\udffb-\udfff]|\ud83e\udd1d\ud83c[\udffb-\udfff]|\ud83e\udd3c\ud83c[\udffb-\udfff]|\ud83d[\udc6b-\udc6d\udc6f\udc8f\udc91]|\ud83e[\udd1d\udd3c])|(?:\ud83d[\udc68\udc69]|\ud83e\uddd1)(?:\ud83c[\udffb-\udfff])?\u200d(?:\u2695\ufe0f|\u2696\ufe0f|\u2708\ufe0f|\ud83c[\udf3e\udf73\udf7c\udf84\udf93\udfa4\udfa8\udfeb\udfed]|\ud83d[\udcbb\udcbc\udd27\udd2c\ude80\ude92]|\ud83e[\uddaf-\uddb3\uddbc\uddbd\ude70])(?:\u200d\u27a1\ufe0f)?|(?:\ud83c[\udfcb\udfcc]|\ud83d[\udd74\udd75]|\u26f9)((?:\ud83c[\udffb-\udfff]|\ufe0f)\u200d[\u2640\u2642]\ufe0f(?:\u200d\u27a1\ufe0f)?)|(?:\ud83c[\udfc3\udfc4\udfca]|\ud83d[\udc6e\udc70\udc71\udc73\udc77\udc81\udc82\udc86\udc87\ude45-\ude47\ude4b\ude4d\ude4e\udea3\udeb4-\udeb6]|\ud83e[\udd26\udd35\udd37-\udd39\udd3d\udd3e\uddb8\uddb9\uddcd-\uddcf\uddd4\uddd6-\udddd])(?:\ud83c[\udffb-\udfff])?\u200d[\u2640\u2642]\ufe0f(?:\u200d\u27a1\ufe0f)?|(?:\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d\udc66\u200d\ud83d\udc66|\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d\udc67\u200d\ud83d[\udc66\udc67]|\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc66\u200d\ud83d\udc66|\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d[\udc66\udc67]|\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d\udc66\u200d\ud83d\udc66|\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d[\udc66\udc67]|\ud83e\uddd1\u200d\ud83e\uddd1\u200d\ud83e\uddd2\u200d\ud83e\uddd2|\ud83d\udc68\u200d\ud83d\udc66\u200d\ud83d\udc66|\ud83d\udc68\u200d\ud83d\udc67\u200d\ud83d[\udc66\udc67]|\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d[\udc66\udc67]|\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d[\udc66\udc67]|\ud83d\udc69\u200d\ud83d\udc66\u200d\ud83d\udc66|\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d[\udc66\udc67]|\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d[\udc66\udc67]|\ud83e\uddd1\u200d\ud83e\uddd1\u200d\ud83e\uddd2|\ud83e\uddd1\u200d\ud83e\uddd2\u200d\ud83e\uddd2|\ud83c\udff3\ufe0f\u200d\u26a7\ufe0f|\ud83c\udff3\ufe0f\u200d\ud83c\udf08|\ud83d\ude36\u200d\ud83c\udf2b\ufe0f|\u26d3\ufe0f\u200d\ud83d\udca5|\u2764\ufe0f\u200d\ud83d\udd25|\u2764\ufe0f\u200d\ud83e\ude79|\ud83c\udf44\u200d\ud83d\udfeb|\ud83c\udf4b\u200d\ud83d\udfe9|\ud83c\udff4\u200d\u2620\ufe0f|\ud83d\udc15\u200d\ud83e\uddba|\ud83d\udc26\u200d\ud83d\udd25|\ud83d\udc3b\u200d\u2744\ufe0f|\ud83d\udc41\u200d\ud83d\udde8|\ud83d\udc68\u200d\ud83d[\udc66\udc67]|\ud83d\udc69\u200d\ud83d[\udc66\udc67]|\ud83d\ude2e\u200d\ud83d\udca8|\ud83d\ude35\u200d\ud83d\udcab|\ud83d\ude42\u200d\u2194\ufe0f|\ud83d\ude42\u200d\u2195\ufe0f|\ud83e\uddd1\u200d\ud83e\uddd2|\ud83e\uddde\u200d\u2640\ufe0f|\ud83e\uddde\u200d\u2642\ufe0f|\ud83e\udddf\u200d\u2640\ufe0f|\ud83e\udddf\u200d\u2642\ufe0f|\ud83d\udc08\u200d\u2b1b|\ud83d\udc26\u200d\u2b1b)|[#*0-9]\ufe0f?\u20e3|(?:[©®\u2122\u265f]\ufe0f)|(?:\ud83c[\udc04\udd70\udd71\udd7e\udd7f\ude02\ude1a\ude2f\ude37\udf21\udf24-\udf2c\udf36\udf7d\udf96\udf97\udf99-\udf9b\udf9e\udf9f\udfcd\udfce\udfd4-\udfdf\udff3\udff5\udff7]|\ud83d[\udc3f\udc41\udcfd\udd49\udd4a\udd6f\udd70\udd73\udd76-\udd79\udd87\udd8a-\udd8d\udda5\udda8\uddb1\uddb2\uddbc\uddc2-\uddc4\uddd1-\uddd3\udddc-\uddde\udde1\udde3\udde8\uddef\uddf3\uddfa\udecb\udecd-\udecf\udee0-\udee5\udee9\udef0\udef3]|[\u203c\u2049\u2139\u2194-\u2199\u21a9\u21aa\u231a\u231b\u2328\u23cf\u23ed-\u23ef\u23f1\u23f2\u23f8-\u23fa\u24c2\u25aa\u25ab\u25b6\u25c0\u25fb-\u25fe\u2600-\u2604\u260e\u2611\u2614\u2615\u2618\u2620\u2622\u2623\u2626\u262a\u262e\u262f\u2638-\u263a\u2640\u2642\u2648-\u2653\u2660\u2663\u2665\u2666\u2668\u267b\u267f\u2692-\u2697\u2699\u269b\u269c\u26a0\u26a1\u26a7\u26aa\u26ab\u26b0\u26b1\u26bd\u26be\u26c4\u26c5\u26c8\u26cf\u26d1\u26d3\u26d4\u26e9\u26ea\u26f0-\u26f5\u26f8\u26fa\u26fd\u2702\u2708\u2709\u270f\u2712\u2714\u2716\u271d\u2721\u2733\u2734\u2744\u2747\u2757\u2763\u2764\u27a1\u2934\u2935\u2b05-\u2b07\u2b1b\u2b1c\u2b50\u2b55\u3030\u303d\u3297\u3299])(?:\ufe0f|(?!\ufe0e))|(?:(?:\ud83c[\udfcb\udfcc]|\ud83d[\udd74\udd75\udd90]|\ud83e\udef0|[\u261d\u26f7\u26f9\u270c\u270d])(?:\ufe0f|(?!\ufe0e))|(?:\ud83c\udfc3|\ud83d\udeb6|\ud83e\uddce)(?:\ud83c[\udffb-\udfff])?(?:\u200d\u27a1\ufe0f)?|(?:\ud83c[\udf85\udfc2\udfc4\udfc7\udfca]|\ud83d[\udc42\udc43\udc46-\udc50\udc66-\udc69\udc6e\udc70-\udc78\udc7c\udc81-\udc83\udc85-\udc87\udcaa\udd7a\udd95\udd96\ude45-\ude47\ude4b-\ude4f\udea3\udeb4\udeb5\udec0\udecc]|\ud83e[\udd0c\udd0f\udd18-\udd1c\udd1e\udd1f\udd26\udd30-\udd39\udd3d\udd3e\udd77\uddb5\uddb6\uddb8\uddb9\uddbb\uddcd\uddcf\uddd1-\udddd\udec3-\udec5\udef1-\udef8]|[\u270a\u270b]))(?:\ud83c[\udffb-\udfff])?|(?:\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc65\udb40\udc6e\udb40\udc67\udb40\udc7f|\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc73\udb40\udc63\udb40\udc74\udb40\udc7f|\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc77\udb40\udc6c\udb40\udc73\udb40\udc7f|\ud83c\udde6\ud83c[\udde8-\uddec\uddee\uddf1\uddf2\uddf4\uddf6-\uddfa\uddfc\uddfd\uddff]|\ud83c\udde7\ud83c[\udde6\udde7\udde9-\uddef\uddf1-\uddf4\uddf6-\uddf9\uddfb\uddfc\uddfe\uddff]|\ud83c\udde8\ud83c[\udde6\udde8\udde9\uddeb-\uddee\uddf0-\uddf7\uddfa-\uddff]|\ud83c\udde9\ud83c[\uddea\uddec\uddef\uddf0\uddf2\uddf4\uddff]|\ud83c\uddea\ud83c[\udde6\udde8\uddea\uddec\udded\uddf7-\uddfa]|\ud83c\uddeb\ud83c[\uddee-\uddf0\uddf2\uddf4\uddf7]|\ud83c\uddec\ud83c[\udde6\udde7\udde9-\uddee\uddf1-\uddf3\uddf5-\uddfa\uddfc\uddfe]|\ud83c\udded\ud83c[\uddf0\uddf2\uddf3\uddf7\uddf9\uddfa]|\ud83c\uddee\ud83c[\udde8-\uddea\uddf1-\uddf4\uddf6-\uddf9]|\ud83c\uddef\ud83c[\uddea\uddf2\uddf4\uddf5]|\ud83c\uddf0\ud83c[\uddea\uddec-\uddee\uddf2\uddf3\uddf5\uddf7\uddfc\uddfe\uddff]|\ud83c\uddf1\ud83c[\udde6-\udde8\uddee\uddf0\uddf7-\uddfb\uddfe]|\ud83c\uddf2\ud83c[\udde6\udde8-\udded\uddf0-\uddff]|\ud83c\uddf3\ud83c[\udde6\udde8\uddea-\uddec\uddee\uddf1\uddf4\uddf5\uddf7\uddfa\uddff]|\ud83c\uddf4\ud83c\uddf2|\ud83c\uddf5\ud83c[\udde6\uddea-\udded\uddf0-\uddf3\uddf7-\uddf9\uddfc\uddfe]|\ud83c\uddf6\ud83c\udde6|\ud83c\uddf7\ud83c[\uddea\uddf4\uddf8\uddfa\uddfc]|\ud83c\uddf8\ud83c[\udde6-\uddea\uddec-\uddf4\uddf7-\uddf9\uddfb\uddfd-\uddff]|\ud83c\uddf9\ud83c[\udde6\udde8\udde9\uddeb-\udded\uddef-\uddf4\uddf7\uddf9\uddfb\uddfc\uddff]|\ud83c\uddfa\ud83c[\udde6\uddec\uddf2\uddf3\uddf8\uddfe\uddff]|\ud83c\uddfb\ud83c[\udde6\udde8\uddea\uddec\uddee\uddf3\uddfa]|\ud83c\uddfc\ud83c[\uddeb\uddf8]|\ud83c\uddfd\ud83c\uddf0|\ud83c\uddfe\ud83c[\uddea\uddf9]|\ud83c\uddff\ud83c[\udde6\uddf2\uddfc]|\ud83c[\udccf\udd8e\udd91-\udd9a\udde6-\uddff\ude01\ude32-\ude36\ude38-\ude3a\ude50\ude51\udf00-\udf20\udf2d-\udf35\udf37-\udf7c\udf7e-\udf84\udf86-\udf93\udfa0-\udfc1\udfc5\udfc6\udfc8\udfc9\udfcf-\udfd3\udfe0-\udff0\udff4\udff8-\udfff]|\ud83d[\udc00-\udc3e\udc40\udc44\udc45\udc51-\udc65\udc6a\udc79-\udc7b\udc7d-\udc80\udc84\udc88-\udc8e\udc90\udc92-\udca9\udcab-\udcfc\udcff-\udd3d\udd4b-\udd4e\udd50-\udd67\udda4\uddfb-\ude44\ude48-\ude4a\ude80-\udea2\udea4-\udeb3\udeb7-\udebf\udec1-\udec5\uded0-\uded2\uded5-\uded8\udedc-\udedf\udeeb\udeec\udef4-\udefc\udfe0-\udfeb\udff0]|\ud83e[\udd0d\udd0e\udd10-\udd17\udd20-\udd25\udd27-\udd2f\udd3a\udd3f-\udd45\udd47-\udd76\udd78-\uddb4\uddb7\uddba\uddbc-\uddcc\uddd0\uddde-\uddff\ude70-\ude7c\ude80-\ude8a\ude8e-\udec2\udec6\udec8\udecd-\udedc\udedf-\udeea\udeef]|[\u23e9-\u23ec\u23f0\u23f3\u267e\u26ce\u2705\u2728\u274c\u274e\u2753-\u2755\u2795-\u2797\u27b0\u27bf\ue50a])|\ufe0f""")) }

/*
 * JVM regular expressions treat UTF-16 surrogate pairs as a single atom while
 * the upstream JavaScript expression addresses their code units separately.
 * Swapping the surrogate and private-use blocks keeps every index unchanged and
 * gives all Kotlin targets the same code-unit matching semantics.
 */
private fun encodeEmojiPattern(pattern: String): String {
    val result = StringBuilder(pattern.length)
    var index = 0
    while (index < pattern.length) {
        if (index + 5 < pattern.length && pattern[index] == '\\' && pattern[index + 1] == 'u') {
            val value = pattern.substring(index + 2, index + 6).toIntOrNull(16)
            if (value != null) {
                val encoded = encodeEmojiCodeUnit(value.toChar()).code.toString(16).padStart(4, '0')
                result.append("\\u").append(encoded)
                index += 6
                continue
            }
        }
        result.append(pattern[index])
        index++
    }
    return result.toString()
}

private fun encodeEmojiCodeUnit(value: Char): Char =
    when (value) {
        in '\uD800'..'\uDFFF' -> (value.code + 0x0800).toChar()
        in '\uE000'..'\uE7FF' -> (value.code - 0x0800).toChar()
        else -> value
    }

internal fun encodeUnicodeEmojiInput(input: String): String {
    val first = input.indexOfFirst { it in '\uD800'..'\uDFFF' || it in '\uE000'..'\uE7FF' }
    if (first < 0) return input
    val result = StringBuilder(input.length)
    result.append(input, 0, first)
    for (index in first until input.length) result.append(encodeEmojiCodeUnit(input[index]))
    return result.toString()
}

/** Returns -1 when the exact regex fallback is required. */
internal fun unicodeEmojiFastLengthAt(
    input: String,
    index: Int,
): Int {
    val first = input[index]
    val codePointLength =
        if (first.isHighSurrogate() && index + 1 < input.length && input[index + 1].isLowSurrogate()) 2 else 1
    val codePoint =
        if (codePointLength == 2) {
            0x10000 + ((first.code - 0xD800) shl 10) + (input[index + 1].code - 0xDC00)
        } else {
            first.code
        }
    val keycap = codePoint == '#'.code || codePoint == '*'.code || codePoint in '0'.code..'9'.code
    if (!keycap && !couldStartUnicodeEmoji(codePoint)) return 0

    var single = false
    var supportsVariationSelector = false
    if (!keycap) {
        single = codePoint inCodePointRanges singleEmojiRanges
        if (!single) {
            supportsVariationSelector = codePoint inCodePointRanges emojiWithVariationSelectorRanges
            if (!supportsVariationSelector) return 0
        }
    }

    val nextIndex = index + codePointLength
    val next = if (nextIndex < input.length) input[nextIndex] else '\u0000'
    val afterVariationSelector = if (nextIndex + 1 < input.length) input[nextIndex + 1] else '\u0000'
    val nextCodePoint = codePointAtOrNull(input, nextIndex)

    if (keycap) {
        return when {
            next == '\u20E3' -> 2
            next == '\uFE0F' && afterVariationSelector == '\u20E3' -> 3
            else -> 0
        }
    }

    val needsFullMatch =
        next == '\u200D' ||
            (next == '\uFE0F' && afterVariationSelector == '\u200D') ||
            next == '\uDB40' ||
            nextCodePoint in 0x1F3FB..0x1F3FF ||
            (codePoint in 0x1F1E6..0x1F1FF && nextCodePoint in 0x1F1E6..0x1F1FF)
    if (needsFullMatch) return -1

    if (next == '\uFE0E') {
        return if (single && codePoint inCodePointRanges emojiBeforeTextSelectorRanges) codePointLength else 0
    }
    if (
        next == '\uFE0F' &&
        (supportsVariationSelector || codePoint inCodePointRanges emojiWithVariationSelectorRanges)
    ) {
        return codePointLength + 1
    }
    return if (single) codePointLength else 0
}

internal fun unicodeEmojiComplexLengthAt(
    encodedInput: String,
    index: Int,
): Int {
    val match = unicodeEmojiRegex.matchAt(encodedInput, index) ?: return 0
    val length = match.range.last - match.range.first + 1
    if (length == 1 && encodedInput[index] == '\uFE0F') return 0
    return length
}

private fun codePointAtOrNull(
    input: String,
    index: Int,
): Int {
    if (index !in input.indices) return -1
    val first = input[index]
    val second = if (index + 1 < input.length) input[index + 1] else '\u0000'
    return if (first.isHighSurrogate() && second.isLowSurrogate()) {
        0x10000 + ((first.code - 0xD800) shl 10) + (second.code - 0xDC00)
    } else {
        first.code
    }
}

private fun couldStartUnicodeEmoji(codePoint: Int): Boolean =
    codePoint == 0xA9 ||
        codePoint == 0xAE ||
        codePoint in 0x203C..0x3299 ||
        codePoint == 0xE50A ||
        codePoint in 0x1F004..0x1FAF8

private infix fun Int.inCodePointRanges(ranges: IntArray): Boolean {
    var low = 0
    var high = ranges.size / 2 - 1
    while (low <= high) {
        val middle = (low + high) ushr 1
        val start = ranges[middle * 2]
        val end = ranges[middle * 2 + 1]
        when {
            this < start -> high = middle - 1
            this > end -> low = middle + 1
            else -> return true
        }
    }
    return false
}

private val singleEmojiRanges =
    intArrayOf(
    0x203c, 0x203c, 0x2049, 0x2049, 0x2139, 0x2139, 0x2194, 0x2199, 0x21a9, 0x21aa,
    0x231a, 0x231b, 0x2328, 0x2328, 0x23cf, 0x23cf, 0x23e9, 0x23f3, 0x23f8, 0x23fa,
    0x24c2, 0x24c2, 0x25aa, 0x25ab, 0x25b6, 0x25b6, 0x25c0, 0x25c0, 0x25fb, 0x25fe,
    0x2600, 0x2604, 0x260e, 0x260e, 0x2611, 0x2611, 0x2614, 0x2615, 0x2618, 0x2618,
    0x261d, 0x261d, 0x2620, 0x2620, 0x2622, 0x2623, 0x2626, 0x2626, 0x262a, 0x262a,
    0x262e, 0x262f, 0x2638, 0x263a, 0x2640, 0x2640, 0x2642, 0x2642, 0x2648, 0x2653,
    0x2660, 0x2660, 0x2663, 0x2663, 0x2665, 0x2666, 0x2668, 0x2668, 0x267b, 0x267b,
    0x267e, 0x267f, 0x2692, 0x2697, 0x2699, 0x2699, 0x269b, 0x269c, 0x26a0, 0x26a1,
    0x26a7, 0x26a7, 0x26aa, 0x26ab, 0x26b0, 0x26b1, 0x26bd, 0x26be, 0x26c4, 0x26c5,
    0x26c8, 0x26c8, 0x26ce, 0x26cf, 0x26d1, 0x26d1, 0x26d3, 0x26d4, 0x26e9, 0x26ea,
    0x26f0, 0x26f5, 0x26f7, 0x26fa, 0x26fd, 0x26fd, 0x2702, 0x2702, 0x2705, 0x2705,
    0x2708, 0x270d, 0x270f, 0x270f, 0x2712, 0x2712, 0x2714, 0x2714, 0x2716, 0x2716,
    0x271d, 0x271d, 0x2721, 0x2721, 0x2728, 0x2728, 0x2733, 0x2734, 0x2744, 0x2744,
    0x2747, 0x2747, 0x274c, 0x274c, 0x274e, 0x274e, 0x2753, 0x2755, 0x2757, 0x2757,
    0x2763, 0x2764, 0x2795, 0x2797, 0x27a1, 0x27a1, 0x27b0, 0x27b0, 0x27bf, 0x27bf,
    0x2934, 0x2935, 0x2b05, 0x2b07, 0x2b1b, 0x2b1c, 0x2b50, 0x2b50, 0x2b55, 0x2b55,
    0x3030, 0x3030, 0x303d, 0x303d, 0x3297, 0x3297, 0x3299, 0x3299, 0xe50a, 0xe50a,
    0x1f004, 0x1f004, 0x1f0cf, 0x1f0cf, 0x1f170, 0x1f171, 0x1f17e, 0x1f17f, 0x1f18e, 0x1f18e,
    0x1f191, 0x1f19a, 0x1f1e6, 0x1f1ff, 0x1f201, 0x1f202, 0x1f21a, 0x1f21a, 0x1f22f, 0x1f22f,
    0x1f232, 0x1f23a, 0x1f250, 0x1f251, 0x1f300, 0x1f321, 0x1f324, 0x1f393, 0x1f396, 0x1f397,
    0x1f399, 0x1f39b, 0x1f39e, 0x1f3f0, 0x1f3f3, 0x1f3f5, 0x1f3f7, 0x1f3ff, 0x1f400, 0x1f4fd,
    0x1f4ff, 0x1f53d, 0x1f549, 0x1f54e, 0x1f550, 0x1f567, 0x1f56f, 0x1f570, 0x1f573, 0x1f57a,
    0x1f587, 0x1f587, 0x1f58a, 0x1f58d, 0x1f590, 0x1f590, 0x1f595, 0x1f596, 0x1f5a4, 0x1f5a5,
    0x1f5a8, 0x1f5a8, 0x1f5b1, 0x1f5b2, 0x1f5bc, 0x1f5bc, 0x1f5c2, 0x1f5c4, 0x1f5d1, 0x1f5d3,
    0x1f5dc, 0x1f5de, 0x1f5e1, 0x1f5e1, 0x1f5e3, 0x1f5e3, 0x1f5e8, 0x1f5e8, 0x1f5ef, 0x1f5ef,
    0x1f5f3, 0x1f5f3, 0x1f5fa, 0x1f64f, 0x1f680, 0x1f6c5, 0x1f6cb, 0x1f6d2, 0x1f6d5, 0x1f6d8,
    0x1f6dc, 0x1f6e5, 0x1f6e9, 0x1f6e9, 0x1f6eb, 0x1f6ec, 0x1f6f0, 0x1f6f0, 0x1f6f3, 0x1f6fc,
    0x1f7e0, 0x1f7eb, 0x1f7f0, 0x1f7f0, 0x1f90c, 0x1f93a, 0x1f93c, 0x1f945, 0x1f947, 0x1f9ff,
    0x1fa70, 0x1fa7c, 0x1fa80, 0x1fa8a, 0x1fa8e, 0x1fac6, 0x1fac8, 0x1fac8,
    0x1facd, 0x1fadc, 0x1fadf, 0x1faea, 0x1faef, 0x1faf8,
    )

private val emojiWithVariationSelectorRanges =
    intArrayOf(
    0xa9, 0xa9, 0xae, 0xae, 0x203c, 0x203c, 0x2049, 0x2049, 0x2122, 0x2122,
    0x2139, 0x2139, 0x2194, 0x2199, 0x21a9, 0x21aa, 0x231a, 0x231b, 0x2328, 0x2328,
    0x23cf, 0x23cf, 0x23ed, 0x23ef, 0x23f1, 0x23f2, 0x23f8, 0x23fa, 0x24c2, 0x24c2,
    0x25aa, 0x25ab, 0x25b6, 0x25b6, 0x25c0, 0x25c0, 0x25fb, 0x25fe, 0x2600, 0x2604,
    0x260e, 0x260e, 0x2611, 0x2611, 0x2614, 0x2615, 0x2618, 0x2618, 0x261d, 0x261d,
    0x2620, 0x2620, 0x2622, 0x2623, 0x2626, 0x2626, 0x262a, 0x262a, 0x262e, 0x262f,
    0x2638, 0x263a, 0x2640, 0x2640, 0x2642, 0x2642, 0x2648, 0x2653, 0x265f, 0x2660,
    0x2663, 0x2663, 0x2665, 0x2666, 0x2668, 0x2668, 0x267b, 0x267b, 0x267f, 0x267f,
    0x2692, 0x2697, 0x2699, 0x2699, 0x269b, 0x269c, 0x26a0, 0x26a1, 0x26a7, 0x26a7,
    0x26aa, 0x26ab, 0x26b0, 0x26b1, 0x26bd, 0x26be, 0x26c4, 0x26c5, 0x26c8, 0x26c8,
    0x26cf, 0x26cf, 0x26d1, 0x26d1, 0x26d3, 0x26d4, 0x26e9, 0x26ea, 0x26f0, 0x26f5,
    0x26f7, 0x26fa, 0x26fd, 0x26fd, 0x2702, 0x2702, 0x2708, 0x2709, 0x270c, 0x270d,
    0x270f, 0x270f, 0x2712, 0x2712, 0x2714, 0x2714, 0x2716, 0x2716, 0x271d, 0x271d,
    0x2721, 0x2721, 0x2733, 0x2734, 0x2744, 0x2744, 0x2747, 0x2747, 0x2757, 0x2757,
    0x2763, 0x2764, 0x27a1, 0x27a1, 0x2934, 0x2935, 0x2b05, 0x2b07, 0x2b1b, 0x2b1c,
    0x2b50, 0x2b50, 0x2b55, 0x2b55, 0x3030, 0x3030, 0x303d, 0x303d, 0x3297, 0x3297,
    0x3299, 0x3299, 0x1f004, 0x1f004, 0x1f170, 0x1f171, 0x1f17e, 0x1f17f, 0x1f202, 0x1f202,
    0x1f21a, 0x1f21a, 0x1f22f, 0x1f22f, 0x1f237, 0x1f237, 0x1f321, 0x1f321, 0x1f324, 0x1f32c,
    0x1f336, 0x1f336, 0x1f37d, 0x1f37d, 0x1f396, 0x1f397, 0x1f399, 0x1f39b, 0x1f39e, 0x1f39f,
    0x1f3cb, 0x1f3ce, 0x1f3d4, 0x1f3df, 0x1f3f3, 0x1f3f3, 0x1f3f5, 0x1f3f5, 0x1f3f7, 0x1f3f7,
    0x1f43f, 0x1f43f, 0x1f441, 0x1f441, 0x1f4fd, 0x1f4fd, 0x1f549, 0x1f54a, 0x1f56f, 0x1f570,
    0x1f573, 0x1f579, 0x1f587, 0x1f587, 0x1f58a, 0x1f58d, 0x1f590, 0x1f590, 0x1f5a5, 0x1f5a5,
    0x1f5a8, 0x1f5a8, 0x1f5b1, 0x1f5b2, 0x1f5bc, 0x1f5bc, 0x1f5c2, 0x1f5c4, 0x1f5d1, 0x1f5d3,
    0x1f5dc, 0x1f5de, 0x1f5e1, 0x1f5e1, 0x1f5e3, 0x1f5e3, 0x1f5e8, 0x1f5e8, 0x1f5ef, 0x1f5ef,
    0x1f5f3, 0x1f5f3, 0x1f5fa, 0x1f5fa, 0x1f6cb, 0x1f6cb, 0x1f6cd, 0x1f6cf, 0x1f6e0, 0x1f6e5,
    0x1f6e9, 0x1f6e9, 0x1f6f0, 0x1f6f0, 0x1f6f3, 0x1f6f3, 0x1faf0, 0x1faf0,
    )

private val emojiBeforeTextSelectorRanges =
    intArrayOf(
    0x23e9, 0x23ec, 0x23f0, 0x23f0, 0x23f3, 0x23f3, 0x267e, 0x267e, 0x26ce, 0x26ce,
    0x2705, 0x2705, 0x270a, 0x270b, 0x2728, 0x2728, 0x274c, 0x274c, 0x274e, 0x274e,
    0x2753, 0x2755, 0x2795, 0x2797, 0x27b0, 0x27b0, 0x27bf, 0x27bf, 0xe50a, 0xe50a,
    0xfe0f, 0xfe0f, 0x1f0cf, 0x1f0cf, 0x1f18e, 0x1f18e, 0x1f191, 0x1f19a, 0x1f1e6, 0x1f1ff,
    0x1f201, 0x1f201, 0x1f232, 0x1f236, 0x1f238, 0x1f23a, 0x1f250, 0x1f251, 0x1f300, 0x1f320,
    0x1f32d, 0x1f335, 0x1f337, 0x1f37c, 0x1f37e, 0x1f393, 0x1f3a0, 0x1f3ca, 0x1f3cf, 0x1f3d3,
    0x1f3e0, 0x1f3f0, 0x1f3f4, 0x1f3f4, 0x1f3f8, 0x1f3ff, 0x1f400, 0x1f43e, 0x1f440, 0x1f440,
    0x1f442, 0x1f4fc, 0x1f4ff, 0x1f53d, 0x1f54b, 0x1f54e, 0x1f550, 0x1f567, 0x1f57a, 0x1f57a,
    0x1f595, 0x1f596, 0x1f5a4, 0x1f5a4, 0x1f5fb, 0x1f64f, 0x1f680, 0x1f6c5, 0x1f6cc, 0x1f6cc,
    0x1f6d0, 0x1f6d2, 0x1f6d5, 0x1f6d8, 0x1f6dc, 0x1f6df, 0x1f6eb, 0x1f6ec, 0x1f6f4, 0x1f6fc,
    0x1f7e0, 0x1f7eb, 0x1f7f0, 0x1f7f0, 0x1f90c, 0x1f93a, 0x1f93c, 0x1f945, 0x1f947, 0x1f9ff,
    0x1fa70, 0x1fa7c, 0x1fa80, 0x1fa8a, 0x1fa8e, 0x1fac6, 0x1fac8, 0x1fac8,
    0x1facd, 0x1fadc, 0x1fadf, 0x1faea, 0x1faef, 0x1faef, 0x1faf1, 0x1faf8,
    )
