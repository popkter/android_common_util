package com.pop.common.log_ext

/*
 * Copyright (C) 2018 Drake, https://github.com/liangjingkanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.math.min


@Suppress("MemberVisibilityCanBePrivate")
/**
 * @property tag 默认日志标签
 * @property enabled 日志全局开关
 * @property logHooks 日志拦截器
 */
object LogCat {

    enum class Type {
        VERBOSE, DEBUG, INFO, WARN, ERROR, WTF
    }

    /** 日志默认标签 */
    var tag = "Logger"

    /** 是否启用日志 */
    var enabled = true

    /** 日志是否显示代码位置 */
    var traceEnabled = true

    /** 日志的Hook钩子 */
    val logHooks by lazy { ArrayList<LogHook>() }

    /**
     * @param enabled 是否启用日志
     * @param tag 日志默认标签
     */
    fun setDebug(enabled: Boolean, tag: String = LogCat.tag) {
        LogCat.enabled = enabled
        LogCat.tag = tag
    }

    //<editor-fold desc="Hook">
    /**
     * 添加日志拦截器
     */
    fun addHook(hook: LogHook) {
        logHooks.add(hook)
    }

    /**
     * 删除日志拦截器
     */
    fun removeHook(hook: LogHook) {
        logHooks.remove(hook)
    }
    //</editor-fold>

    // <editor-fold desc="Log">

    @JvmOverloads
    @JvmStatic
    fun v(
        msg: Any?,
        tag: String = LogCat.tag,
        tr: Throwable? = null,
        occurred: Throwable? = Exception()
    ) {

        print(Type.VERBOSE, msg, tag, tr, occurred)
    }

    @JvmOverloads
    @JvmStatic
    fun i(
        msg: Any?,
        tag: String = LogCat.tag,
        tr: Throwable? = null,
        occurred: Throwable? = Exception()
    ) {
        print(Type.INFO, msg, tag, tr, occurred)
    }

    @JvmOverloads
    @JvmStatic
    fun d(
        msg: Any?,
        tag: String = LogCat.tag,
        tr: Throwable? = null,
        occurred: Throwable? = Exception()
    ) {
        print(Type.DEBUG, msg, tag, tr, occurred)
    }

    @JvmOverloads
    @JvmStatic
    fun w(
        msg: Any?,
        tag: String = LogCat.tag,
        tr: Throwable? = null,
        occurred: Throwable? = Exception()
    ) {
        print(Type.WARN, msg, tag, tr, occurred)
    }

    @JvmOverloads
    @JvmStatic
    fun e(
        msg: Any?,
        tag: String = LogCat.tag,
        tr: Throwable? = null,
        occurred: Throwable? = Exception()
    ) {
        print(Type.ERROR, msg, tag, tr, occurred)
    }

    @JvmOverloads
    @JvmStatic
    fun e(
        tr: Throwable?,
        tag: String = LogCat.tag,
        occurred: Throwable? = Exception(),
        msg: Any? = "",
    ) {
        print(Type.ERROR, msg, tag, tr, occurred)
    }

    @JvmOverloads
    @JvmStatic
    fun wtf(
        msg: Any?,
        tag: String = LogCat.tag,
        tr: Throwable? = null,
        occurred: Throwable? = Exception()
    ) {
        print(Type.WTF, msg, tag, tr, occurred)
    }

    /**
     * 输出日志
     * 如果[msg]和[occurred]为空或者[tag]为空将不会输出日志, 拦截器
     *
     * @param type 日志等级
     * @param msg 日志信息
     * @param tag 日志标签
     * @param occurred 日志异常
     */
    private fun print(
        type: Type = Type.INFO,
        msg: Any? = null,
        tag: String = LogCat.tag,
        tr: Throwable? = null,
        occurred: Throwable? = Exception()
    ) {
        if (!enabled || msg == null) return

        var realTag = tag
        var message = msg.toString()

        val info = LogInfo(type, message, tag, tr, occurred)
        for (logHook in logHooks) {
            logHook.hook(info)
            if (info.msg == null) return
        }

        if (traceEnabled && occurred != null) {
            occurred.stackTrace.getOrNull(1)?.run {
                message += " ...($fileName:$lineNumber)"
                if (realTag == "Logger"){
                    realTag = fileName.substringBefore('.')
                }
            }
        }
        val max = 3800
        val length = message.length
        if (length > max) {
            synchronized(this) {
                var startIndex = 0
                var endIndex = max
                while (startIndex < length) {
                    endIndex = min(length, endIndex)
                    val substring = message.substring(startIndex, endIndex)
                    log(type, substring, realTag, tr)
                    startIndex += max
                    endIndex += max
                }
            }
        } else {
            log(type, message, realTag, tr)
        }
    }

    /**
     * JSON格式化输出日志
     * @param tag 日志标签
     * @param msg 日志信息
     * @param type 日志类型
     * @param occurred 日志发生位置
     */
    @JvmOverloads
    @JvmStatic
    fun json(
        json: Any?,
        tag: String = LogCat.tag,
        msg: String = "",
        type: Type = Type.INFO,
        occurred: Throwable? = Exception()
    ) {
        if (!enabled || json == null) return

        var message = json.toString()

        val occurredMsg = if (traceEnabled && occurred != null) {
            occurred.stackTrace.getOrNull(1)?.run { " ($fileName:$lineNumber)" }
        } else ""

        if (message.isBlank()) {
            print(type, "$msg$occurredMsg\n$message", tag, occurred = null)
            return
        }

        val tokener = JSONTokener(message)
        val obj = try {
            tokener.nextValue()
        } catch (e: Exception) {
            "Parse json error"
        }

        message = when (obj) {
            is JSONObject -> obj.toString(2)
            is JSONArray -> obj.toString(2)
            else -> obj.toString()
        }

        print(type, "$msg$occurredMsg\n$message", tag, occurred = null)
    }

    private fun log(type: Type, msg: String, tag: String, tr: Throwable?) {
        when (type) {
            Type.VERBOSE -> Log.v(tag, msg, tr)
            Type.DEBUG -> Log.d(tag, msg, tr)
            Type.INFO -> Log.i(tag, msg, tr)
            Type.WARN -> Log.w(tag, msg, tr)
            Type.ERROR -> Log.e(tag, msg, tr)
            Type.WTF -> Log.wtf(tag, msg, tr)
        }
    }
    // </editor-fold>
}