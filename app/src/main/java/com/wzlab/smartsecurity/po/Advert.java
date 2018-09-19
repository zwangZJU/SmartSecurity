package com.wzlab.smartsecurity.po;

/**
 * Created by wzlab on 2018/9/18.
 */


/**
 * <b>广告表[advert]数据持久化对象</b>
 * <p>
 * 注意:此文件由AOS平台自动生成-禁止手工修改。
 * </p>
 *
 * @author duanchongfeng
 * @date 2017-04-21 20:56:28
 */
public class Advert  {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String advert_id;

    /**
     * 名称
     */
    private String name;

    /**
     * 短名称
     */
    private String shortname;

    /**
     * 状态，0：使用中，1：禁用
     */
    private String status;

    /**
     * 跳转地址
     */
    private String url;

    /**
     * 图片地址
     */
    private String img_url;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否删除，0：未删除，1：删除
     */
    private String is_del;

    /**
     * 创建时间
     */
    private String create_date;

    /**
     * 操作人ID
     */
    private String oper_id;


    /**
     * 主键
     *
     * @return advert_id
     */
    public String getAdvert_id() {
        return advert_id;
    }

    /**
     * 名称
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 短名称
     *
     * @return shortname
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * 状态，0：使用中，1：禁用
     *
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 跳转地址
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 图片地址
     *
     * @return img_url
     */
    public String getImg_url() {
        return img_url;
    }

    /**
     * 排序
     *
     * @return sort
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 是否删除，0：未删除，1：删除
     *
     * @return is_del
     */
    public String getIs_del() {
        return is_del;
    }

    /**
     * 创建时间
     *
     * @return create_date
     */
    public String getCreate_date() {
        return create_date;
    }

    /**
     * 操作人ID
     *
     * @return oper_id
     */
    public String getOper_id() {
        return oper_id;
    }


    /**
     * 主键
     *
     * @param advert_id
     */
    public void setAdvert_id(String advert_id) {
        this.advert_id = advert_id;
    }

    /**
     * 名称
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 短名称
     *
     * @param shortname
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * 状态，0：使用中，1：禁用
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 跳转地址
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 图片地址
     *
     * @param img_url
     */
    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    /**
     * 排序
     *
     * @param sort
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 是否删除，0：未删除，1：删除
     *
     * @param is_del
     */
    public void setIs_del(String is_del) {
        this.is_del = is_del;
    }

    /**
     * 创建时间
     *
     * @param create_date
     */
    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    /**
     * 操作人ID
     *
     * @param oper_id
     */
    public void setOper_id(String oper_id) {
        this.oper_id = oper_id;
    }


}