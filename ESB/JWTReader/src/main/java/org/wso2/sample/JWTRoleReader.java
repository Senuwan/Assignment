package org.wso2.sample;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * JWTRoleReader class will accept the JWT token and decodes the JWT payload
 * Class will set the role and the email to synapse properties
 *
 */

public class JWTRoleReader extends AbstractMediator {

	private MessageContext context;
	private void setMessageContext(MessageContext msg){
		context = msg;
	}
	public String getJWT_JSON_Array() {
		return JWT_JSON_Array;
	}
	public void setJWT_JSON_Array(String JWT_JSON_Array) {
		this.JWT_JSON_Array = JWT_JSON_Array;
	}
	private  String JWT_JSON_Array;
	public boolean mediate(MessageContext context) {

		setMessageContext(context);
		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) context).getAxis2MessageContext();
		Object headerObj = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		@SuppressWarnings("unchecked")
		Map headers = (Map) headerObj;
		// JWT header element
		String jwt_assertion = (String) headers.get("x-jwt-assertion");

		String [] jwt_assertion_items = jwt_assertion.split("\\.");
		// Get the JWT Payload
		byte[] byteArray =  Base64Utils.decode(jwt_assertion_items[1]);
		String outLoad;
		try {
			outLoad = new String(byteArray, "UTF-8");
			net.minidev.json.parser.JSONParser jp = new net.minidev.json.parser.JSONParser();
			// JSON representation of the incoming JWT token payload.
			JSONObject jo = (JSONObject) jp.parse(outLoad);
			JSONArray roleJasonArray = getTheChildJsonObjectArray(jo,"http://wso2.org/claims/role");

			setROleNameToSynapseMessageContext(roleJasonArray,"role_name");

			context.setProperty("emailaddress",(String)jo.get("http://wso2.org/claims/emailaddress"));



			//JSONArray emailJasonArray = getTheChildJsonObjectArray(jo,"http://wso2.org/claims/role");



		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return true;
	}


	/**
	 *getTheChildJsonObjectArray intend to return an Jsonarray nased on the property which
	 * defined using jsonProperty
	 * @param jsonObj Jason object that consists of key and value pairs
	 * @param jsonProperty the String expressing the
	 * @return JSONArray
	 */
	private JSONArray getTheChildJsonObjectArray (JSONObject jsonObj, String jsonProperty){
		JSONArray array = (JSONArray) jsonObj.get(jsonProperty);
		return array;

	}


	/**
	 *
	 * @param array
	 * @param synapsePropertyName
	 */
	private void setROleNameToSynapseMessageContext(JSONArray array,String synapsePropertyName){

		// Assigned created User Roles

		for(Object value:array){
			if(value instanceof String) {
				if(value.equals("AdminRole")){
					context.setProperty(synapsePropertyName,value);

					return;
				}else if(value.equals("physiciansRole")){
					context.setProperty(synapsePropertyName,value);
					return;
				} else if(value.equals("patientRole")) {

					context.setProperty(synapsePropertyName,value);

					return;
				}
			}

		}


	}




}