package club.plutomc.plutoproject.messaging.api

interface Messenger {

    /**
     * 获取指定名称的频道对象。
     * @param name 频道名称。
     * @return 频道对象。
     */
    fun get(name: String): Channel

    /**
     * 获取是否存在指定名称的频道。
     * @param name 频道名称
     * @return 布尔值，若存在则为 true，不存在则为 false。
     */
    fun exist(name: String): Boolean

    /**
     * 移除指定名称的频道。
     * @param name 频道名称。
     */
    fun remove(name: String)

    /**
     * 获取操作器。
     * @return 操作器。
     */
    // abstract fun getOperator(): Operator

    /**
     * 获取整个消息网络中的成员（包括自己）。
     * @return 消息成员集合。
     */
    fun getMembers(): Collection<Member>

    /**
     * 获取当前的 Member。
     * @return 当前 Member。
     */
    fun getMember(): Member

    /**
     * 获取代理端的 Member 对象。
     * @return 代理端 Member 对象。
     */
    fun getProxy(): Member?

    /**
     * 向所有成员广播该服务端是代理端。
     */
    // abstract fun announceProxy()

    fun getInternalChannel(): Channel

}