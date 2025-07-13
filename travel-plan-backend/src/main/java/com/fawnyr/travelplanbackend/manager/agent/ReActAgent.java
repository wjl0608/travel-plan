package com.fawnyr.travelplanbackend.manager.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract String think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        String content = "";
        try {
            String thinkContent = think();
            content+=thinkContent;
            if (thinkContent.startsWith("0")) {
                return "思考完成 - 无需行动。\n" + "思考结果如下：" + content;
            }
            content+=act();
            return content;
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
