package poly.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;

import poly.dto.ApiDTO;
import poly.dto.BigCgDTO;
import poly.dto.DongDTO;
import poly.dto.GugunDTO;
import poly.dto.MidCgDTO;
import poly.dto.ReviewDTO;
import poly.dto.SidoDTO;
import poly.dto.SmallCgDTO;
import poly.service.IBigDataService;
import poly.service.ICafeService;
import poly.service.IReviewService;
import poly.util.CmmUtil;
import poly.util.FormatString;
import poly.util.HttpUtil;
import poly.util.OpenApiXml;
import poly.util.StringUtil;

@Controller
public class BigDataController {
	private Logger log = Logger.getLogger(this.getClass());
	@Resource(name="BigDataService")
	private IBigDataService bigDataService;

	
	@RequestMapping(value="bigData/bigData")
	public String bigData() {
		return "/bigData/bigData";
	}
	//저장된 db를 통해 시도 조회
	@RequestMapping(value="/sido/sidoSearch")
	public  @ResponseBody List<SidoDTO> sidoSearch(HttpServletRequest req) throws Exception{
		log.info("sidosearch start:"+this.getClass());
		List <SidoDTO> sList= new ArrayList<>();
		sList = bigDataService.getSidoList();
		log.info("sidosearch end:"+this.getClass());
		return sList;
		
	}
	//저장된 db를 통해 구군 조회
	@RequestMapping(value="/gugun/gugunSearch")
	public  @ResponseBody List<GugunDTO> gugunSearch(HttpServletRequest req) throws Exception{
		log.info("gugunSearch start:"+this.getClass());
		List <GugunDTO> gList= new ArrayList<>();
		String sido=req.getParameter("sido");//시도 코드
		gList = bigDataService.getGugunSidoList(sido);//쿼리문으로 시도에 맞는 구군
		log.info("gugunSearch end:"+this.getClass());
		return gList;
	}
	//저장된 db를 통해 동읍면 조회
	@RequestMapping(value="/dong/dongSearch")
	public  @ResponseBody List<DongDTO> dongSearch(HttpServletRequest req) throws Exception{
		log.info("dongSearch start:"+this.getClass());
		List <DongDTO> dList= new ArrayList<>();
		String gugun=req.getParameter("gugun");//구군 코드
		dList = bigDataService.getDongGugunList(gugun);//쿼리문으로 시도에 맞는 구군
		log.info("dongSearch end:"+this.getClass());
		return dList;
	}
	//저장된 db를 통해 대분류 조회
	@RequestMapping(value="/cgBusiness/bigSearch")
	public @ResponseBody List<BigCgDTO> bigSearch() throws Exception{
		log.info("bigCgSearch start:"+this.getClass());
		List<BigCgDTO> bList = new ArrayList<>();
		bList = bigDataService.getBigCgList();
		log.info("bigCgSearch end:"+this.getClass());
		return bList;
	}
	//저장된 db를 통해 중분류 조회 
	@RequestMapping (value="/cgBusiness/midSearch")
	public @ResponseBody List<MidCgDTO> midSearch(HttpServletRequest req) throws Exception{
		log.info ("midCgSearch start: "+this.getClass());
		List<MidCgDTO> mList =new ArrayList<>();
		String bigCode = req.getParameter("bigCode");
		mList = bigDataService.getMidCgBigCgList(bigCode);
		log.info ("midCgSearch end: "+this.getClass());
		return mList;
	}

	//저장된 db를 통해 소분류 조회 
		@RequestMapping (value="/cgBusiness/smallSearch")
		public @ResponseBody List<SmallCgDTO> smallSearch(HttpServletRequest req) throws Exception{
			log.info ("smallCgSearch start: "+this.getClass());
			List<SmallCgDTO> sList =new ArrayList<>();
			String midCode = req.getParameter("midCode");
			sList = bigDataService.getSmallCgMidCgList(midCode);
			log.info ("smallCgSearch end: "+this.getClass());
			return sList;
		}
		
		@RequestMapping(value="/bigData/bsCgDistribution")
		public @ResponseBody Map<String, Object> bsCgDistribution(HttpServletRequest req) throws Exception{
			log.info("search bsCgDistribution start: "+this.getClass());
			//divId  인 지역 분류코드와  key 업종코드값 불러와야됨
			String divId=req.getParameter("divId");
			log.info("search bsCgDistribution req divId:"+divId);
			String key=req.getParameter("key");
			log.info("search bsCgDistribution req key:"+key);
			String ServiceKey="DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D";
			String url = "http://apis.data.go.kr/B553077/api/open/sdsc/storeListInUpjong?divId="+divId+"&key="+key+"&ServiceKey="+ServiceKey+"&type=json";
			
			HashMap<String, String> hashmapJson = new HashMap<String, String>();
			HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
			
			
			try{
				String charSet = "EUC-KR";
				
				HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
				if ("200".equals(hashmapResponse.get("httpStatus"))){
					String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
					hashmapRes = StringUtil.JsonStringToObject(responseBody);
				}else{
					hashmapRes.put("REP_CODE", "9999");
					hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
				}
			}catch (Exception e){
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
			
				log.info("search  bsCgDistribution  end: "+this.getClass());
			  /*  Iterator<String> i =  hashmapRes.keySet().iterator();
		         while(i.hasNext()) {
		        	i.next();
		  
		         }*/
				return hashmapRes;
				//여기서 apiDTO 에도 저장해야됨

		}

		@RequestMapping(value="/bigData/cafeLocAnalysis")
		public @ResponseBody Map<String,Object> cafeLocAnalysis(HttpServletRequest req) throws Exception{
			log.info("cafeLocAnalysis start:"+this.getClass());
			
			String cx = req.getParameter("cx");//중심점 경도
			log.info("cafeLoc Ans cx:"+cx);
			String cy = req.getParameter("cy");//중심점 위도
			log.info("cafeLoc Ans cy:"+cy);
			String radius = req.getParameter("radius");//반경
			log.info("cafeLoc Ans radius:"+radius);
			String ServiceKey="DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D";
			//String bgCg= req.getParameter("bgCg");
			
			
			String url ="http://apis.data.go.kr/B553077/api/open/sdsc/storeListInRadius?"
					+ "radius="+radius
					+ "&cx="+cx
					+ "&cy="+cy
					+ "&ServiceKey="+ServiceKey
					+"&type=json"
					+"&numOfRows=1000";
			
			HashMap<String, String> hashmapJson = new HashMap<String, String>();
			HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
			
			try{
				String charSet = "EUC-KR";
				
				HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
				if ("200".equals(hashmapResponse.get("httpStatus"))){
					String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
					hashmapRes = StringUtil.JsonStringToObject(responseBody);
				}else{
					hashmapRes.put("REP_CODE", "9999");
					hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
				}
			}catch (Exception e){
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
			
			 /*   Iterator<String> i =  hashmapRes.keySet().iterator();
		         while(i.hasNext()) {
		        	 i.next();
		          String key1 = i.next();
		            System.out.println(hashmapRes.get(key1));
		         }*/
		 		log.info("cafeLocAnalysis end:"+this.getClass());
				return hashmapRes;
		}
		
		
/////////////////////////////////////////////////////////////////////////////////////////////////////업종별 분포도 시작/////////////////////////////////////////////////////////////////////////////////	


			//저장된 db를 통해 시도 카페 개수 결과값
			
			@RequestMapping(value="/bigData/allCafeSido")
			public @ResponseBody List<SidoDTO> allCafeSido(HttpServletRequest req) throws Exception{
				log.info("전체지도 상권개수 시작 :"+ this.getClass());
				List<SidoDTO> sList=new ArrayList<>();
				sList=bigDataService.getSidoList();
				
				log.info("전체지도 상권개수 끝 :"+ this.getClass());
				return sList;
				 
				}
			//저장된 db를 통해 구군 카페 개수 결과값
			
			@RequestMapping(value="/bigData/allCafeGugun")
			public @ResponseBody List<GugunDTO> allCafeGugun(HttpServletRequest req) throws Exception{
				log.info("전체지도  구군 상권개수 시작 :"+ this.getClass());
				String sido = req.getParameter("addrSido");
				List<GugunDTO> gList=new ArrayList<>();
				gList=bigDataService.getGugunSidoList(sido);
			
				log.info("전체지도 구군 상권개수 끝 :"+ this.getClass());
				return gList;
				 
				}
		//저장된 db를 통해 동읍명 카페 개수 결과값
			
			@RequestMapping(value="/bigData/allCafeDong")
			public @ResponseBody List<DongDTO> allCafeDong(HttpServletRequest req) throws Exception{
				log.info("전체지도 동읍면 상권개수 시작 :"+ this.getClass());
				String gugun=req.getParameter("gugun");//시도 코드
				List<DongDTO> dList=new ArrayList<>();
				dList=bigDataService.getDongGugunList(gugun);			
				log.info("전체지도 동읍면 상권개수 끝 :"+ this.getClass());
				return dList;
				 
		
			}
			
			
			

		/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*	//저장된 db를 통해 시도 카페 개수 결과값
		
		@RequestMapping(value="bigData/allCafeSido")
		public @ResponseBody List<SidoDTO> allCafeSido(HttpServletRequest req) throws Exception{
			log.info("전체지도 상권개수 시작 :"+ this.getClass());
			List<SidoDTO> sList=new ArrayList<>();
			sList=bigDataService.getSidoList();
			
			log.info("전체지도 상권개수 시작 :"+ this.getClass());
			return sList;
			 
			}
	//저장된 db를 통해 구군 카페 개수 결과값
		
		@RequestMapping(value="bigData/allCafeGugun")
		public @ResponseBody List<GugunDTO> allCafeGugun(HttpServletRequest req) throws Exception{
			log.info("전체지도  구군 상권개수 시작 :"+ this.getClass());
			String sido=req.getParameter("sido");//시도 코드
			List<GugunDTO> gList=new ArrayList<>();
			gList=bigDataService.getGugunSidoList(sido);			
			log.info("전체지도 구군 상권개수 시작 :"+ this.getClass());
			return gList;
			 
			}
	//저장된 db를 통해 동읍명 카페 개수 결과값
		
		@RequestMapping(value="bigData/allCafeDong")
		public @ResponseBody List<DongDTO> allCafeDong(HttpServletRequest req) throws Exception{
			log.info("전체지도 동읍면 상권개수 시작 :"+ this.getClass());
			String gugun=req.getParameter("gugun");//시도 코드
			List<DongDTO> dList=new ArrayList<>();
			dList=bigDataService.getDongGugunList(gugun);			
			log.info("전체지도 동읍면 상권개수 시작 :"+ this.getClass());
			return dList;
			 
			}
		*/
		
		
		
	
	//api를 통한 시도  조회
	/*@RequestMapping(value="sido/sidoSearch")
	public @ResponseBody List<Object> sidoSearch(HttpServletRequest req) throws Exception{
		log.info("sidosearch start:"+this.getClass());
		String url ="http://apis.data.go.kr/B553077/api/open/sdsc/baroApi?resId=dong&catId=mega&signguCd=00&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json";
		List<Object> mList=new ArrayList<>();
		HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
		
		try{
			String charSet = "EUC-KR";
			
			HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
			if ("200".equals(hashmapResponse.get("httpStatus"))){
				String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
				hashmapRes = StringUtil.JsonStringToObject(responseBody);
				String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
			    JSONParser parser = new JSONParser();
			    Object obj = parser.parse(bodyJSON); 
			    JSONObject jsonObj = (JSONObject) obj;
			    JSONArray bodyJSONArray = (JSONArray) jsonObj.get("items");
			   for(int i=0 ; i<bodyJSONArray.size();i++) {
				   	mList.add(bodyJSONArray.get(i));
			   }
			}else{
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
		}catch (Exception e){
			hashmapRes.put("REP_CODE", "9999");
			hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
		}
		
	 		log.info("sido end:"+this.getClass());
			return mList;
	}
		//api를 통한 구군 조회
	@RequestMapping(value="gugun/gugunSearch")
	public @ResponseBody List<Object> gugunSearch(HttpServletRequest req) throws Exception{
		log.info("gugunsearch start:"+this.getClass());
		String sido=req.getParameter("sido");//시도 코드
		String url ="http://apis.data.go.kr/B553077/api/open/sdsc/"
				+ "baroApi?resId=dong&catId=cty&ctprvnCd="+sido
				+ "&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json";
		List<Object> mList=new ArrayList<>();
		HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
		
		try{
			String charSet = "EUC-KR";
			
			HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
			if ("200".equals(hashmapResponse.get("httpStatus"))){
				String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
				hashmapRes = StringUtil.JsonStringToObject(responseBody);
				String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
			    JSONParser parser = new JSONParser();
			    Object obj = parser.parse(bodyJSON); 
			    JSONObject jsonObj = (JSONObject) obj;
			    JSONArray bodyJSONArray = (JSONArray) jsonObj.get("items");
			   for(int i=0 ; i<bodyJSONArray.size();i++) {
				   	mList.add(bodyJSONArray.get(i));
			   }
			}else{
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
		}catch (Exception e){
			hashmapRes.put("REP_CODE", "9999");
			hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
		}
		
	 		log.info("gugun end:"+this.getClass());
			return mList;
	}
	
	*/
	
	

	
	
	
	
/*	//시도 조회
	@RequestMapping(value="sido/sidoSearch")
	public @ResponseBody List<ApiDTO> sido(HttpServletRequest req) throws Exception{
	log.info("search sido start: "+this.getClass());
	List<ApiDTO> sidoList = new ArrayList<>();
	try {
		DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse("http://apis.data.go.kr/B553077/api/open/sdsc/baroApi?resId=dong&catId=mega&signguCd=00&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D");
		doc.getDocumentElement().normalize();
		NodeList list = doc.getElementsByTagName("item");//태그 시작위치
		
		for(int temp = 0; temp < list.getLength(); temp++){
			Node node = list.item(temp);
			if(node.getNodeType()==Node.ELEMENT_NODE){
				Element element = (Element)node;
				ApiDTO apiDTO=new ApiDTO();
				apiDTO.setCtprvnNm(OpenApiXml.getTagValue("ctprvnNm",element));
				apiDTO.setCtprvnCd(OpenApiXml.getTagValue("ctprvnCd",element));
			
				sidoList.add(apiDTO);
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	log.info("search sido end: "+this.getClass());
	
		return sidoList;
	}
	*/
	
	/*//구군 조회
		@RequestMapping(value="gugun/gugunSearch")
		public @ResponseBody List<ApiDTO>  gugun(HttpServletRequest req) throws Exception{
		log.info("search gugun start: "+this.getClass());
		String sido=req.getParameter("sido");//시도 코드
		List<ApiDTO> gugunList = new ArrayList<>();
		//xml 파싱
		try {
			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse("http://apis.data.go.kr/B553077/api/open/sdsc/"
					+ "baroApi?resId=dong&catId=cty&ctprvnCd="+sido
					+ "&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D");
			
			doc.getDocumentElement().normalize();
			
			NodeList list = doc.getElementsByTagName("item");//태그 시작위치
			
			for(int temp = 0; temp < list.getLength(); temp++){
				Node node = list.item(temp);
				if(node.getNodeType()==Node.ELEMENT_NODE){
					Element element = (Element)node;
					ApiDTO apiDTO=new ApiDTO();
					//여기서는 시도 번호나 명을 세팅해야 나중에 select 조회 할때 쓸수 있음
					apiDTO.setCtprvnCd(OpenApiXml.getTagValue("ctprvnCd",element));
					apiDTO.setSignguCd(OpenApiXml.getTagValue("signguCd",element));//시군구코드번호 세팅
					apiDTO.setSignguNm(OpenApiXml.getTagValue("signguNm",element));//시군구 명 세팅
					gugunList.add(apiDTO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("search gugun end: "+this.getClass());
		
			return gugunList;
		}*/
		/*//동면읍 조회
			@RequestMapping(value="dong/dongSearch")
			public @ResponseBody List<ApiDTO>  dongEubMyun(HttpServletRequest req) throws Exception{
				log.info("search dong start: "+this.getClass());
				String gugun=req.getParameter("gugun");//시군구 코드
				log.info("search dong gugun code:"+gugun);
				List<ApiDTO> dongList = new ArrayList<>();
				//xml 파싱
				try {
					DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse("http://apis.data.go.kr/B553077/api/open/sdsc/baroApi?resId=dong&catId=admi"
							+ "&signguCd="+gugun
							+ "&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D");
					
					doc.getDocumentElement().normalize();
					NodeList list = doc.getElementsByTagName("item");//태그 시작위치
					for(int temp = 0; temp < list.getLength(); temp++){
						Node node = list.item(temp);
						if(node.getNodeType()==Node.ELEMENT_NODE){
							Element element = (Element)node;
							ApiDTO apiDTO=new ApiDTO();
							//여기서는 시도 번호나 명을 세팅해야 나중에 select 조회 할때 쓸수 있음
							apiDTO.setSignguCd(OpenApiXml.getTagValue("signguCd",element));//시군구 코드
							apiDTO.setAdongCd(OpenApiXml.getTagValue("adongCd",element));//행정동코드
							apiDTO.setAdongNm(OpenApiXml.getTagValue("adongNm",element));//행정동명 
							dongList.add(apiDTO);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("search dong end: "+this.getClass());
				
					return dongList;
				}
				
				//dong 조회
	@RequestMapping(value="dong/dongSearch")
	public @ResponseBody List<Object> dongSearch(HttpServletRequest req) throws Exception{
		log.info("dongsearch start:"+this.getClass());
		String gugun=req.getParameter("gugun");//구군코드
		String url ="http://apis.data.go.kr/B553077/api/open/sdsc/baroApi?resId=dong&catId=admi"
				+ "&signguCd="+gugun
				+ "&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json";
		List<Object> mList=new ArrayList<>();
		HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
		
		try{
			String charSet = "EUC-KR";
			
			HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
			if ("200".equals(hashmapResponse.get("httpStatus"))){
				String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
				hashmapRes = StringUtil.JsonStringToObject(responseBody);
				String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
			    JSONParser parser = new JSONParser();
			    Object obj = parser.parse(bodyJSON); 
			    JSONObject jsonObj = (JSONObject) obj;
			    JSONArray bodyJSONArray = (JSONArray) jsonObj.get("items");
			   for(int i=0 ; i<bodyJSONArray.size();i++) {
				   	mList.add(bodyJSONArray.get(i));
			   }
			}else{
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
		}catch (Exception e){
			hashmapRes.put("REP_CODE", "9999");
			hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
		}
		
	 		log.info("dong end:"+this.getClass());
			return mList;
	}
				
				
				
*/				
				
////////////////////////////지역별  업종별 분포도 조회

	
/////////////////대분류 중분류 소분류 api로 받아오는 코드  json 파싱///////////////////////////
/*//대분류
	@RequestMapping(value="cgBusiness/bigSearch")
	public @ResponseBody List<Object> bigSearch(HttpServletRequest req) throws Exception{
		log.info("bigsearch start:"+this.getClass());
		String url ="http://apis.data.go.kr/B553077/api/open/sdsc/largeUpjongList?ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json";
		List<Object> mList=new ArrayList<>();
		HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
		
		try{
			String charSet = "EUC-KR";
			
			HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
			if ("200".equals(hashmapResponse.get("httpStatus"))){
				String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
				hashmapRes = StringUtil.JsonStringToObject(responseBody);
				String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
			    JSONParser parser = new JSONParser();
			    Object obj = parser.parse(bodyJSON); 
			    JSONObject jsonObj = (JSONObject) obj;
			    JSONArray bodyJSONArray = (JSONArray) jsonObj.get("items");
			   for(int i=0 ; i<bodyJSONArray.size();i++) {
				   	mList.add(bodyJSONArray.get(i));
			   }
			}else{
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
		}catch (Exception e){
			hashmapRes.put("REP_CODE", "9999");
			hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
		}
		
	 		log.info("big end:"+this.getClass());
			return mList;
	}
	//중분류
	@RequestMapping(value="cgBusiness/midSearch")
	public @ResponseBody List<Object> midSearch(HttpServletRequest req) throws Exception{
		log.info("search midBusiness start: "+this.getClass());
		String bigCode=req.getParameter("bigCode");
		String url ="http://apis.data.go.kr/B553077/api/open/sdsc/middleUpjongList?indsLclsCd="+bigCode+"&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json";
		List<Object> mList=new ArrayList<>();
		HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
		
		try{
			String charSet = "EUC-KR";
			
			HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
			if ("200".equals(hashmapResponse.get("httpStatus"))){
				String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
				hashmapRes = StringUtil.JsonStringToObject(responseBody);
				String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
			    JSONParser parser = new JSONParser();
			    Object obj = parser.parse(bodyJSON); 
			    JSONObject jsonObj = (JSONObject) obj;
			    JSONArray bodyJSONArray = (JSONArray) jsonObj.get("items");
			   for(int i=0 ; i<bodyJSONArray.size();i++) {
				   	mList.add(bodyJSONArray.get(i));
			   }
			}else{
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
		}catch (Exception e){
			hashmapRes.put("REP_CODE", "9999");
			hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
		}
		
	 		log.info("midcg end:"+this.getClass());
			return mList;
	}
	
//소분류
		@RequestMapping(value="cgBusiness/smallSearch")
		public @ResponseBody List<Object> smallSearch(HttpServletRequest req) throws Exception{
			log.info("search smallBusiness start: "+this.getClass());
			String bigCode=req.getParameter("bigCode");
			String midCode=req.getParameter("midCode");
			String url ="http://apis.data.go.kr/B553077/api/open/sdsc/smallUpjongList?indsLclsCd="+bigCode
					+ "&indsMclsCd="+midCode
					+ "&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json";
			List<Object> mList=new ArrayList<>();
			HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
			
			try{
				String charSet = "EUC-KR";
				
				HashMap<String, String> hashmapResponse = (HashMap<String, String>) HttpUtil.callURLGet(url,  charSet);
				if ("200".equals(hashmapResponse.get("httpStatus"))){
					String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
					hashmapRes = StringUtil.JsonStringToObject(responseBody);
					String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
				    JSONParser parser = new JSONParser();
				    Object obj = parser.parse(bodyJSON); 
				    JSONObject jsonObj = (JSONObject) obj;
				    JSONArray bodyJSONArray = (JSONArray) jsonObj.get("items");
				   for(int i=0 ; i<bodyJSONArray.size();i++) {
					   	mList.add(bodyJSONArray.get(i));
				   }
				}else{
					hashmapRes.put("REP_CODE", "9999");
					hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
				}
			}catch (Exception e){
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
			
		 		log.info("smallcg end:"+this.getClass());
				return mList;
		}
	*/
	
	//여기는 xml 파싱
/*//대분류 조회/////////////////////////////
	@RequestMapping(value="cgBusiness/bigSearch")
	public @ResponseBody List<ApiDTO>  bigBusiness(HttpServletRequest req) throws Exception{
		log.info("search bigBusiness start: "+this.getClass());
		List<ApiDTO> bigBusinessList = new ArrayList<>();
				//xml 파싱
			try {
				DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse("http://apis.data.go.kr/B553077/api/open/sdsc/largeUpjongList?"
						+ "ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D");
					
				doc.getDocumentElement().normalize();
					
				NodeList list = doc.getElementsByTagName("item");//태그 시작위치
					
				for(int temp = 0; temp < list.getLength(); temp++){
					Node node = list.item(temp);
					if(node.getNodeType()==Node.ELEMENT_NODE){
						Element element = (Element)node;
						//String ctprvnNm=OpenApi.getTagValue("ctprvnNm",element);
						ApiDTO apiDTO=new ApiDTO();
							//여기서는 시도 번호나 명을 세팅해야 나중에 select 조회 할때 쓸수 있음
						apiDTO.setIndsLclsCd(OpenApiXml.getTagValue("indsLclsCd",element));//대분류코드
						apiDTO.setIndsLclsNm(OpenApiXml.getTagValue("indsLclsNm",element));//대분류명 
						//확인
						log.info("대분류 코드 get:"+apiDTO.getIndsLclsCd());
						log.info("대분류 명 get:"+apiDTO.getIndsLclsNm());
						bigBusinessList.add(apiDTO);
							
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("search bigBusiness end: "+this.getClass());
				
					return bigBusinessList;
				}
*/				
/*	//중분류 조회/////////////////////////////		
				
	@RequestMapping(value="cgBusiness/midSearch")
	public @ResponseBody List<ApiDTO>  midBusiness(HttpServletRequest req) throws Exception{
		log.info("search midBusiness start: "+this.getClass());
		String bigCode=req.getParameter("bigCode");
		log.info("search mid req bigCode:"+bigCode);
		List<ApiDTO> midBusinessList = new ArrayList<>();
				//xml 파싱
			try {
				DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse("http://apis.data.go.kr/B553077/api/open/sdsc/middleUpjongList?indsLclsCd="+bigCode
						+ "&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D");
					
				doc.getDocumentElement().normalize();
					
				NodeList list = doc.getElementsByTagName("item");//태그 시작위치
					
				for(int temp = 0; temp < list.getLength(); temp++){
					Node node = list.item(temp);
					if(node.getNodeType()==Node.ELEMENT_NODE){
						Element element = (Element)node;
						//String ctprvnNm=OpenApi.getTagValue("ctprvnNm",element);
						ApiDTO apiDTO=new ApiDTO();
							//여기서는 시도 번호나 명을 세팅해야 나중에 select 조회 할때 쓸수 있음
						
						apiDTO.setIndsLclsCd(OpenApiXml.getTagValue("indsLclsCd",element));//대분류코드
						apiDTO.setIndsMclsCd(OpenApiXml.getTagValue("indsMclsCd",element));//중분류코드 
						apiDTO.setIndsMclsNm(OpenApiXml.getTagValue("indsMclsNm",element));//중분류명 
						//확인
						log.info("중분류 코드 get:"+apiDTO.getIndsMclsCd());
						log.info("중분류 명 get:"+apiDTO.getIndsMclsNm());
						midBusinessList.add(apiDTO);
							
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("search midBusiness end: "+this.getClass());
				
					return midBusinessList;
				}*/
	
	/*//소분류 조회
	
	@RequestMapping(value="cgBusiness/smallSearch")
	public @ResponseBody List<ApiDTO>  smallBusiness(HttpServletRequest req) throws Exception{
	log.info("search smallBusiness start: "+this.getClass());
	String bigCode=req.getParameter("bigCode");
	log.info("search small req bigCode:"+bigCode);
	String midCode=req.getParameter("midCode");
	log.info("search small req midCode:"+midCode);
	List<ApiDTO> smallBusinessList = new ArrayList<>();
		//xml 파싱
	try {
		DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse("http://apis.data.go.kr/B553077/api/open/sdsc/smallUpjongList?indsLclsCd="+bigCode
				+ "&indsMclsCd="+midCode
				+ "&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D");
			
		doc.getDocumentElement().normalize();
			
		//System.out.println("Root element : "+doc.getDocumentElement().getNodeName());
		NodeList list = doc.getElementsByTagName("item");//태그 시작위치
		//System.out.println("-----------------------");
			
		for(int temp = 0; temp < list.getLength(); temp++){
			Node node = list.item(temp);
			if(node.getNodeType()==Node.ELEMENT_NODE){
				Element element = (Element)node;
				//String ctprvnNm=OpenApi.getTagValue("ctprvnNm",element);
				ApiDTO apiDTO=new ApiDTO();
					//여기서는 시도 번호나 명을 세팅해야 나중에 select 조회 할때 쓸수 있음
				
				apiDTO.setIndsMclsCd(OpenApiXml.getTagValue("indsMclsCd",element));//중분류코드
				apiDTO.setIndsSclsCd(OpenApiXml.getTagValue("indsSclsCd",element));//소분류코드 
				apiDTO.setIndsSclsNm(OpenApiXml.getTagValue("indsSclsNm",element));//소분류명 
				//확인
				log.info("중분류 코드 get:"+apiDTO.getIndsSclsCd());
				log.info("중분류 명 get:"+apiDTO.getIndsSclsNm());
				smallBusinessList.add(apiDTO);
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("search midBusiness end: "+this.getClass());
		
			return smallBusinessList;
		}*/
	//업종별 분포도 서울 몇개 경기도 몇개 전라도 몇개 리스트로 대중소 시도, 구군 ,동 9개의 경우의수 일거같음
	//semas openapi 활용가이드  13) 업종별 상가업소 조회 오퍼레이션
	//divId 는 분류 코드 key 업종코드값
	//ApiDTO 에  필요한 값 dto 명 써주기
/////////////////////////////////////////////////////////////////////////////////////////////////////상권밀집도///////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////////////////상권밀집도 시작/////////////////////////////////////////////////////////////
	/*
	  
	 //"http://apis.data.go.kr/B553077/api/open/sdsc/baroApi?resId=dong&catId=mega&signguCd=00&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json
//전국카페 api 받아와서 하는것
		@RequestMapping(value="bigData/allCafeSido",consumes="application/json" ,method=RequestMethod.POST)
		public @ResponseBody List<Map<String,String>> allCafeSido(@RequestBody List<Map<String,Object>> allSido) throws Exception{
		log.info("전체지도 상권개수 시작 :"+ this.getClass());
		//원래는 string으로 받아와서 json 형태로 다시 하려하
		String ctprvnCd="";
		String ctprvnNm="";
		String totalCount="";
		//String bodyJSON2 = StringUtil.ObjectToJsonString(responseBody2);
		//log.info(bodyJSON2);
		List<Map<String,String>> mList=new ArrayList<>();
		
		HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
		
		try{
		
		for(int i = 0 ; i<allSido.size();i++) {
		
		HashMap<String, String> hashmapJson = new HashMap<String, String>();
		//log.info(allSido.get(i));
		//log.info(allSido.get(i).get("ctprvnCd"));
		ctprvnCd=allSido.get(i).get("ctprvnCd").toString();
		String charSet = "EUC-KR";
		HashMap<String, String> hashmapResponse = (HashMap<String, String>) 
		HttpUtil.callURLGet("http://apis.data.go.kr/B553077/api/open/sdsc/storeListInDong?divId=ctprvnCd&key="+ctprvnCd+"&indsLclsCd=Q&indsMclsCd=Q12&indsSclsCd=Q12A01&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json&numOfRows=20",  charSet);
		if ("200".equals(hashmapResponse.get("httpStatus"))){
		String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
		hashmapRes = StringUtil.JsonStringToObject(responseBody);
		String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(bodyJSON); 
		JSONObject jsonObj = (JSONObject) obj;
		
		ctprvnNm =allSido.get(i).get("ctprvnNm").toString();
		totalCount= jsonObj.get("totalCount").toString();
		hashmapJson.put("ctprvnCd",ctprvnCd);
		hashmapJson.put("ctprvnNm",ctprvnNm);
		hashmapJson.put("totalCount",totalCount);
		mList.add(hashmapJson);
		
		}else{
		hashmapRes.put("REP_CODE", "9999");
		hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
		}
		}  
		}catch (Exception e){
		hashmapRes.put("REP_CODE", "9999");
		hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
		}
		
		//while문의 끝
		log.info("전체지도 상권개수 끝 :"+ this.getClass());
		return mList;
		
		} 
	 	public @ResponseBody List<Map<String,String>> allCafeGugun(@RequestBody List<Map<String,Object>> allGugun) throws Exception{
			log.info("구군지도 상권개수 시작 :"+ this.getClass());
			//원래는 string으로 받아와서 json 형태로 다시 하려하
			String signguCd="";
			String signguNm="";
			String totalCount="";
			//String bodyJSON2 = StringUtil.ObjectToJsonString(responseBody2);
			//log.info(bodyJSON2);
			List<Map<String,String>> mList=new ArrayList<>();
			
			HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
			    
			try{
				
				for(int i = 0 ; i<allGugun.size();i++) {
					HashMap<String, String> hashmapJson = new HashMap<String, String>();
					signguCd=allGugun.get(i).get("signguCd").toString();
				String charSet = "EUC-KR";
				HashMap<String, String> hashmapResponse = (HashMap<String, String>) 
						HttpUtil.callURLGet("http://apis.data.go.kr/B553077/api/open/sdsc/storeListInDong?divId=signguCd&key="+signguCd+"&indsLclsCd=Q&indsMclsCd=Q12&indsSclsCd=Q12A01&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json&numOfRows=20",  charSet);
				if ("200".equals(hashmapResponse.get("httpStatus"))){
					String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
					hashmapRes = StringUtil.JsonStringToObject(responseBody);
					String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
				    JSONParser parser = new JSONParser();
				    Object obj = parser.parse(bodyJSON); 
				    JSONObject jsonObj = (JSONObject) obj;

				    signguNm =allGugun.get(i).get("signguNm").toString();
				    totalCount= jsonObj.get("totalCount").toString();
				    hashmapJson.put("signguCd",signguCd);
				    hashmapJson.put("signguNm",signguNm);
				    hashmapJson.put("totalCount",totalCount);
				    mList.add(hashmapJson);
				
				}else{
					hashmapRes.put("REP_CODE", "9999");
					hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
				}
			}  
			}catch (Exception e){
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
		  
			//while문의 끝
			log.info("구군지도 상권개수 끝 :"+ this.getClass());
			return mList;
		}
		@RequestMapping(value="bigData/allCafeDong",consumes="application/json" ,method=RequestMethod.POST)
		public @ResponseBody List<Map<String,String>> allCafeDong(@RequestBody List<Map<String,Object>> allDong) throws Exception{
			log.info("동지도 상권개수 시작 :"+ this.getClass());
			//원래는 string으로 받아와서 json 형태로 다시 하려하
			String adongCd="";
			String adongNm="";
			String totalCount="";
			//String bodyJSON2 = StringUtil.ObjectToJsonString(responseBody2);
			//log.info(bodyJSON2);
			List<Map<String,String>> mList=new ArrayList<>();
			
			HashMap<String, Object> hashmapRes = new HashMap<String, Object>();
			    
			try{
				
				for(int i = 0 ; i<allDong.size();i++) {
					HashMap<String, String> hashmapJson = new HashMap<String, String>();
					adongCd=allDong.get(i).get("adongCd").toString();
				String charSet = "EUC-KR";
				HashMap<String, String> hashmapResponse = (HashMap<String, String>) 
						HttpUtil.callURLGet("http://apis.data.go.kr/B553077/api/open/sdsc/storeListInDong?divId=adongCd&key="+adongCd+"&indsLclsCd=Q&indsMclsCd=Q12&indsSclsCd=Q12A01&ServiceKey=DCERWRgTB%2BHukgI%2BBfnSKofhO6udoVebyOCM4EEZeBKYhcCOb1xlhG2SaLCqdRChGiduI%2FOYrYUGttvma45Ytw%3D%3D&type=json&numOfRows=20",  charSet);
				if ("200".equals(hashmapResponse.get("httpStatus"))){
					String responseBody = String.valueOf(hashmapResponse.get("responseBody"));
					hashmapRes = StringUtil.JsonStringToObject(responseBody);
					String bodyJSON = StringUtil.ObjectToJsonString(hashmapRes.get("body"));
				    JSONParser parser = new JSONParser();
				    Object obj = parser.parse(bodyJSON); 
				    JSONObject jsonObj = (JSONObject) obj;

				    adongNm =allDong.get(i).get("adongNm").toString();
				    totalCount= jsonObj.get("totalCount").toString();
				    hashmapJson.put("adongCd",adongCd);
				    hashmapJson.put("adongNm",adongNm);
				    hashmapJson.put("totalCount",totalCount);
				    mList.add(hashmapJson);
				
				}else{
					hashmapRes.put("REP_CODE", "9999");
					hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
				}
			}  
			}catch (Exception e){
				hashmapRes.put("REP_CODE", "9999");
				hashmapRes.put("REP_MSG", "오류가 발생했습니다.");
			}
		  
			//while문의 끝
			log.info("동지도 상권개수 끝 :"+ this.getClass());
			return mList;
		}
		
	  
	  */
	
	
	
/////////////////////////////////////////////////////////////////////////////////////////////////////상권밀집도 끝///////////////////////////////////////////////////////////////////////////////




}




	

