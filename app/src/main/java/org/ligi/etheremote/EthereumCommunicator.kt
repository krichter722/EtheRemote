package org.ligi.etheremote

import com.github.arteam.simplejsonrpc.client.JsonRpcClient
import com.github.arteam.simplejsonrpc.client.Transport
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import java.math.BigInteger
import java.util.LinkedHashMap

public class EthereumCommunicator {

    private val rpcClient: JsonRpcClient

    init {
        val client = OkHttpClient()

        rpcClient = JsonRpcClient(object : Transport {
            override fun pass(request2: String): String {
                val settings = App.getSettings()
                val request = Request.Builder().url("http://${settings.getHost()}:${settings.getPort()}").post(RequestBody.create(MediaType.parse("text"), request2)).build()

                val response = client.newCall(request).execute()
                return response.body().string()
            }
        })
    }


    private fun getIntegerFromMethod(eth_blockNumber: String): Int? {
        try {
            val res = getStringFromMethod<String?>(eth_blockNumber)
            return parseStringToInt(res)
        } catch (e: Exception) {
            return null
        }

    }

    private fun parseStringToInt(res: String?): Int {
        if (res!!.startsWith("0x"))
            return Integer.parseInt(res.replace("0x", ""), 16)
        else
            return Integer.parseInt(res)
    }

    private fun parseStringToBigInt(res: String?): BigInteger {
        if (res!!.startsWith("0x"))
            return BigInteger(res.replace("0x", ""), 16)
        else
            return BigInteger(res.replace("0x", ""))
    }


    private fun <T> getStringFromMethod(eth_blockNumber: String): T? {
        try {
            val any = rpcClient.createRequest().method(eth_blockNumber).id(1).execute()
            return any as T;
        } catch (e: Exception) {
            return null
        }

    }

    public fun getBlockNumber(): Int? {
        return getIntegerFromMethod("eth_blockNumber")
    }

    public fun getPeerCount(): Int? {
        return getIntegerFromMethod("net_peerCount")
    }


    public fun getEthVersion(): Int? {
        return getIntegerFromMethod("eth_protocolVersion")
    }

    public fun isMining(): Boolean? {
        return getStringFromMethod("eth_mining")
    }

    public fun getHashRate(): Int? {
        return getIntegerFromMethod("eth_hashrate")
    }

    public fun getGasPrice(): Int? {
        return getIntegerFromMethod("eth_gasPrice")
    }

    public fun getAccounts(): List<String>? {
        return getStringFromMethod("eth_accounts")
    }

    fun getBalance(account: String): BigInteger {
        return parseStringToBigInt(rpcClient.createRequest().method("eth_getBalance").params(account, "latest").id(1).execute() as String)
    }

    fun getBlockByNumber(number: Int): LinkedHashMap<String, Any> {

        return rpcClient.createRequest().method("eth_getBlockByNumber").params("0x${Integer.toHexString(number)}", true).id(1).execute() as LinkedHashMap<String, Any>
    }

    fun sendTransaction(from: String, to: String, amount: String, onSuccess: (msg: String) -> Unit, onError: (msg: String?) -> Unit) {
        try {
            onSuccess(rpcClient.createRequest().method("eth_sendTransaction").params(TransferJSON(from, to, amount)).id(1).execute() as String)
        } catch(e: Exception) {
            onError(e.getMessage())
        }
    }

    inner class TransferJSON(from: String, to: String, amount: String) {

        var from: String = from
        var to: String = to
        var amount: String = amount

    }

}
