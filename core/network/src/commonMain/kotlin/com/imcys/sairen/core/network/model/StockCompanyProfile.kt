package com.imcys.sairen.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 东方财富 F10 公司资料接口中 `jbzl` 节点的数据模型。 */
@Serializable
data class StockCompanyProfile(
    /** 公司全称。 */
    @SerialName("gsmc")
    val companyName: String = "",

    /** 公司英文名称。 */
    @SerialName("ywmc")
    val englishCompanyName: String = "",

    /** 所属行业。 */
    @SerialName("sshy")
    val industry: String = "",

    /** 上市交易所。 */
    @SerialName("ssjys")
    val exchange: String = "",

    /** 公司简介。 */
    @SerialName("gsjj")
    val summary: String = "",

    /** 经营范围。 */
    @SerialName("jyfw")
    val businessScope: String = "",

    /** 董事长。 */
    @SerialName("dsz")
    val chairman: String = "",

    /** 法定代表人。 */
    @SerialName("frdb")
    val legalRepresentative: String = "",

    /** 总经理。 */
    @SerialName("zjl")
    val generalManager: String = "",

    /** 公司官网。 */
    @SerialName("gswz")
    val website: String = "",
) : SRModel
