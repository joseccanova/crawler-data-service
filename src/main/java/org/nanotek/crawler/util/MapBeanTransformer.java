package org.nanotek.crawler.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.Transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public interface MapBeanTransformer<I> extends Transformer<I,Map<String , Object>> {

	 
	
	@Override
	default Map<String , Object> transform(I input) {
		ObjectMapper obj = new ObjectMapper();
		JsonNode node = obj.valueToTree(input);
		Map<String,Object> out = new HashMap<>();
		try {
		      addKeys("", node, out);
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		return out;
	}

	private Map<String, Object> addKeys(String currentPath, JsonNode jsonNode) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		Optional
		.ofNullable(jsonNode)
		.filter(n -> n.isObject())
		.ifPresent(n -> 
		{ 
			ObjectNode objectNode = (ObjectNode) jsonNode;
			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";
		      while (iter.hasNext()) {
		        Map.Entry<String, JsonNode> entry = iter.next();
		        addKeys(pathPrefix + entry.getKey(), entry.getValue(), map);
	      }
		 });
		
		Optional.ofNullable(jsonNode)
		.filter(n->n.isValueNode())
		.ifPresent(n->{
			ValueNode vnode = (ValueNode) n;
			map.put(currentPath, vnode.asText());
		});
		
		Optional.ofNullable(jsonNode)
		.filter(n->n.isArray())
		.ifPresent(n->{
		      ArrayNode arrayNode = (ArrayNode) n;
		      for (int i = 0; i < arrayNode.size(); i++) {
		        addKeys(currentPath + "[" + i + "]", arrayNode.get(i), map);
		      }
		});
		
	    return map;
	  }
	
	private void addKeys(String string, JsonNode jsonNode, Map<String, Object> map) {
		Optional.ofNullable(map).ifPresent(m -> m.putAll(addKeys(string , jsonNode)));
	}


}
