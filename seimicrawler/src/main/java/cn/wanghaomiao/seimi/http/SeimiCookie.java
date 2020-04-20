package cn.wanghaomiao.seimi.http;

import java.util.HashMap;
import java.util.Map;

/**
 * http cookie要素，屏蔽hcclient与okhttp区别
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2016/12/4.
 */
public class SeimiCookie {
    public SeimiCookie(String domain, String path, String name, String value) {
        this.domain = domain;
        this.path = path;
        this.name = name;
        this.value = value;
    }

    private String domain;
    private String path;
    private String name;
    private String value;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	@Override
	public String toString() {
		Map<String, String> cookie = new HashMap<String, String>(4);
		cookie.put("domain", this.domain);
		cookie.put("name", this.name);
		cookie.put("value", this.value);
		cookie.put("path", this.path);
		return cookie.toString();
	}
    
}
