package com.trackcar.admin

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val Navy=Color(0xFF101B31)
private val Panel=Color(0xFF1B2942)
private val Cyan=Color(0xFF36C5D8)
private val Muted=Color(0xFF9AAAC2)

data class FleetDevice(val label:String,val code:String,val model:String,val status:String,val lastSeen:String,val lat:Double?,val lng:Double?)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent { AdminScreen() }}
}

@Composable
private fun AdminScreen(){
 val activity = LocalContext.current as ComponentActivity
 val clipboard = LocalClipboardManager.current
 var key by remember{mutableStateOf("")}
 var status by remember{mutableStateOf("Enter the administrator API key to connect to the fleet.")}
 var code by remember{mutableStateOf("")}
 var vehicleLabel by remember{mutableStateOf("")}
 var devices by remember{mutableStateOf(emptyList<FleetDevice>())}
 var busy by remember{mutableStateOf(false)}
 fun request(path:String,method:String,adminKey:String,body:JSONObject?=null,done:(JSONObject?)->Unit){
  Thread{
   var result:JSONObject?=null
   try{
    val c=URL(Api.BASE+path).openConnection() as HttpURLConnection
    c.requestMethod=method;c.connectTimeout=10000;c.readTimeout=15000
    c.setRequestProperty("Accept","application/json");c.setRequestProperty("x-admin-key",adminKey)
    if(body!=null){c.doOutput=true;c.setRequestProperty("Content-Type","application/json");c.outputStream.use{it.write(body.toString().toByteArray())}}
    val stream=if(c.responseCode in 200..299)c.inputStream else c.errorStream
    result=JSONObject(stream?.bufferedReader()?.use{it.readText()}?: "{}")
    c.disconnect()
   }catch(e:Exception){result=JSONObject().put("error",e.message?:"Network error")}
   activity.runOnUiThread{done(result)}
  }.start()
 }
 fun load(){
  if(key.isBlank()){status="Administrator key is required.";return}
  busy=true;request("/api/admin/devices","GET",key){r->
   busy=false
   if(r==null||r.has("error"))status=r?.optString("error")?:"Connection failed"
   else{
    val arr=r.optJSONArray("devices");val list=mutableListOf<FleetDevice>()
    if(arr!=null)for(i in 0 until arr.length()){val d=arr.getJSONObject(i);list.add(FleetDevice(d.optString("label"),d.optString("device_code"),d.optString("manufacturer")+" "+d.optString("model"),d.optString("status"),d.optString("last_seen_at"),if(d.isNull("last_lat"))null else d.optDouble("last_lat"),if(d.isNull("last_lng"))null else d.optDouble("last_lng")))}
    devices=list;status="Connected. Loaded "+list.size+" devices."
   }
  }
 }
 fun createCode(){
  if(key.isBlank()){status="Administrator key is required.";return}
  busy=true;request("/api/admin/install-links","POST",key,JSONObject().put("expires_in_minutes",1440).put("label",vehicleLabel.trim().ifBlank{"Fleet vehicle installation"})){r->
   busy=false
   if(r==null||r.has("error"))status=r?.optString("error")?:"Could not create install link"
   else{code=r.optString("install_url");status="Secure installation link created. It expires in 24 hours and can be used once."}
  }
 }
 fun revokeLink(){
  if(key.isBlank()||code.isBlank())return
  val token=code.substringAfterLast("/")
  busy=true
  request("/api/admin/install-links","DELETE",key,JSONObject().put("token",token)){r->
   busy=false
   if(r==null||r.has("error"))status=r?.optString("error")?:"Could not revoke link"
   else{code="";status="Installation link revoked."}
  }
 }
 MaterialTheme(colorScheme=darkColorScheme(primary=Cyan,background=Navy,surface=Panel,onBackground=Color.White,onSurface=Color.White)){
  Surface(Modifier.fillMaxSize(),color=Navy){
   LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
    item{
     Text("TRACK CAR",color=Cyan,fontWeight=FontWeight.Bold)
     Text("Fleet Admin",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.headlineMedium)
     Text("Connected fleet control",color=Muted)
    }
    item{
     Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(16.dp)){
      Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
       Text("Administrator access",fontWeight=FontWeight.SemiBold)
       OutlinedTextField(value=key,onValueChange={key=it},label={Text("Admin API key")},singleLine=true,modifier=Modifier.fillMaxWidth())
       OutlinedTextField(value=vehicleLabel,onValueChange={vehicleLabel=it},label={Text("Vehicle name / plate (optional)")},singleLine=true,modifier=Modifier.fillMaxWidth())
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        Button(onClick={ load() },enabled=!busy){Text("Load fleet")}
        Button(onClick={ createCode() },enabled=!busy){Text("Create install link")}
       }
       if(code.isNotBlank()){Text("Installation link",color=Muted);Text(code,color=Cyan,fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={clipboard.setText(AnnotatedString(code))}){Text("Copy link")};Button(onClick={activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,code)},"Send installation link"))}){Text("Share")};Button(onClick={revokeLink()},enabled=!busy){Text("Revoke")}}}
       Text(status,color=Muted)
      }
     }
    }
    item{Text("Devices ("+devices.size+")",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)}
    items(devices){d->
     Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()){
      Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
       Row(verticalAlignment=Alignment.CenterVertically){Text(d.label,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));Text(d.status,color=Cyan)}
       Text(d.model+" · "+d.code,color=Muted,style=MaterialTheme.typography.bodySmall)
       Text("Last seen: "+(d.lastSeen.ifBlank{"Never"}),color=Muted,style=MaterialTheme.typography.bodySmall)
       if(d.lat!=null&&d.lng!=null)Text("GPS: "+d.lat+", "+d.lng,color=Cyan,style=MaterialTheme.typography.bodySmall)
      }
     }
    }
   }
  }
 }
}

private object Api{const val BASE="https://track-car-platform-git-main-otpprovider.vercel.app"}
