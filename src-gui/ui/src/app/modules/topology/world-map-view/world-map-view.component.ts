import { Component, OnInit,Input,  OnChanges, SimpleChanges, AfterViewInit, OnDestroy } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import Map from 'ol/Map';
import View from 'ol/View';
import {Tile as TileLayer,Vector as VectorLayer} from 'ol/layer';
import * as proj from 'ol/proj';
import {Cluster,OSM,Vector as VectorSource} from 'ol/source';
import Feature from 'ol/Feature';
import {Point,LineString} from 'ol/geom';
import Overlay from 'ol/Overlay';
import {singleClick,doubleClick} from 'ol/events/condition';
import {
	Circle as CircleStyle,
	Fill,
	Text,
	Icon,
	Stroke,
	Style,
  } from 'ol/style';
  import {
	Select,
	defaults as defaultInteractions,
  } from 'ol/interaction';

  declare var jQuery: any;
  import { environment } from "../../../../environments/environment";
import { TopologyGraphService } from 'src/app/common/services/topology-graph.service';
@Component({
  selector: 'app-world-map-view',
  templateUrl: './world-map-view.component.html',
  styleUrls: ['./world-map-view.component.css']
})
export class WorldMapViewComponent implements OnInit, AfterViewInit, OnChanges, OnDestroy {
  @Input() data:any;
    map:any;
	linkLayer:any;
	centerLng:number = 0 ;
	centerLat:number =  0;
	markers:any = [];
	graphdata:any;
	links:any; 
	switches:any;
	pops:any=[];
	popLinks:any=[];
	markerSource :any=null;
	clusterLinkLayer:any;
	ClusterLinks:any=[];
	clusterLayer:any;
	clusterLinkSource:any;
	linkSource:any=null;
	clusterSource:any=null;
	linkFeatures:any = [];
	mouseCoordinates:any=null;
	clusterDistance:any=50;
	styleCache = {};
	overlay:any; 
	popInfoOverlay:any;
	container=  document.getElementById('popup');
	content = document.getElementById('popup-content');
	closer = document.getElementById('popup-closer');
	popinfocontainer = document.getElementById('popInfoContainer');
	popinfocontent = document.getElementById('popInfocontent');
	popinfocloser = document.getElementById('popInfocloser');
	// minimise = document.getElementById('popup-minimize');
	// maximise = document.getElementById('popup-maximize');
	graph_loader = document.getElementById('graph_loader');
	default_location:any={
		"pop": "Unknown",
		"datacenter": "Unknown",
		"latitude": 22.8951683,
		"longitude": 147.6138315,
		"country": "TPN",
		"city": "Atlantis"
	};
	selectSingleClick = new Select({
		condition: singleClick, 
	  });
	selectDoubleClick = new Select({
	condition: doubleClick, 
	});
	constructor(private httpClient:HttpClient,private topologyGraphService:TopologyGraphService) { 
  }

	ngOnInit(): void {
		
	}
	getPopLinks(switches,links){
		var switchIds = switches.map((d)=>{
			return d.switch_id;
		});
		var isls = [];
		if(links && links.length){
			links.forEach(link=>{
				if(switchIds.indexOf(link.source_switch) > -1 && switchIds.indexOf(link.target_switch) > -1){
					isls.push(link);
				}
			})
		}
		return isls;
	}

	groupBy(objectArray, property) {
		var self = this;
		return objectArray.reduce((acc, obj) => {
		   var key = obj[property];
		   if(key == null || key == '' || key =='string'){
			   key = "Unknown";
		   }
		   var location = obj.location;
		   if(!(location.latitude && location.longitude)){
				obj.location = self.default_location; 
		   }
		   if (!acc[key]) {
			  acc[key] = [];
		   }		  
		   acc[key].push(obj);
		   return acc;
		}, {});
	 }
	ngOnChanges(change:SimpleChanges){
		if( typeof(change.data)!='undefined' && change.data){
			if(typeof(change.data)!=='undefined' && change.data.currentValue){
			  this.data  = change.data.currentValue;
			  if(this.data && this.data.switch && this.data.switch.length){
				this.links = this.data.isl;
				this.switches = this.data.switch;
				var popWiseData = this.groupBy(this.switches,'pop');
				Object.keys(popWiseData).forEach((key)=>{
					var switchIds = popWiseData[key].map(s=>{ return s.switch_id;});
					var links = this.getPopLinks(popWiseData[key],this.links);
					var d = {"id":key,
							"switches":popWiseData[key],
							"location":popWiseData[key][0].location,
							"links":links,
							"switchIds":switchIds
						};
					this.pops.push(d);
				});
				// fetching the links between pops

				for(var i=0; i < this.pops.length; i++){
					var sourcePop = this.pops[i];
					for(var j=0; j< this.pops.length; j++){
						if(i!=j){
							var targetPop = this.pops[j];
							var lnkObj = this.getLinkObjInPops(sourcePop,targetPop);
							if(lnkObj && lnkObj.hasOwnProperty('source')){
								this.popLinks.push(lnkObj);
							}
						}
					}
				}
			}
			}
			this.initMap();
		  }

		
	}

	ngOnDestroy(){

	}
	ngAfterViewInit(){
		setTimeout(()=> {
			this.map.updateSize();
		}, 100);
	}


  	initMap(){
	  this.overlay = new Overlay({
			element: this.container,
			autoPan: true,
			autoPanAnimation: {
				duration: 250,
			},
		});
		this.popInfoOverlay = new Overlay({
			element: this.popinfocontainer,
			autoPan: true,
			autoPanAnimation: {
				duration: 250,
			},
		});
	  this.map =  new Map({
		layers: [
		  new TileLayer({
			source: new OSM()
		  })
		],
		target: 'world_map',
		overlays: [this.overlay,this.popInfoOverlay],
		view: new View({
		  center: [0, 0],
		  zoom: 2,
		  minZoom: 2,
		  maxZoom:20,
		})
	  });
	this.map.addInteraction(this.selectSingleClick); 
	this.map.addInteraction(this.selectDoubleClick);
	var view = this.map.getView();
	view.setCenter(proj.fromLonLat([this.centerLng, this.centerLat]));
	this.loadEvents();	
	this.loadLinks(this.popLinks);
	this.loadMarkersClusters();	
  }

  loadEvents(){
	var self = this;
	if(this.closer){
		this.closer.onclick= (()=>{
			this.overlay.setPosition(undefined);
			this.closer.blur();
			return false;	
		});
	}

	if(this.popinfocloser){
		this.popinfocloser.onclick= (()=>{
			this.popInfoOverlay.setPosition(undefined);
			this.popinfocloser.blur();
			return false;	
		});
	}

	// if(this.minimise){
	// 	this.minimise.onclick= (()=>{
	// 		console.log('here i m');
	// 		return false;	
	// 	});
	// }

	// if(this.maximise){
	// 	this.maximise.onclick= (()=>{
	// 		console.log('here i m max');
	// 		return false;	
	// 	});
	// }
	this.map.on('pointermove',(evt)=>{
		var pixel = evt.pixel;
		var feature = this.map.forEachFeatureAtPixel(pixel, function(feature) {
			return feature;
		});
		if(feature && feature.values_ && typeof feature.values_.features !='undefined' && feature.values_.features.length ==1){
			var featureValues = feature.values_.features[0].values_;
			if(featureValues && featureValues.type == 'marker'){
				var coordinate = feature.values_.features[0].getGeometry().getCoordinates();
				self.popinfocontent.innerHTML = "";
				var html = "<div class='row'><div class='col-md-12'><div class='form-group'><label><b>Pop: </b></label><span>"+featureValues.id+"</span></div>";
				html+= "<div class='form-group'><label><b>City: </b></label><span>"+featureValues.city+"</span></div><div class='form-group'><label><b>Country: </b></label><span>"+featureValues.country+"</span></div></div></div>";
				self.popinfocontent.innerHTML = html;
				self.popInfoOverlay.setPosition(coordinate);
						
			}
		}else{
			self.popinfocontent.innerHTML = "";
			self.popInfoOverlay.setPosition(undefined);
		}
	})
	var currZoom = this.map.getView().getZoom();

	this.map.on('moveend', (e)=> {
	var newZoom = this.map.getView().getZoom();	
	if (currZoom != newZoom) {
		setTimeout(()=>{
		this.enableLinks();
		this.loadCLusterLinks();
		},500);
	}
	});
	this.map.on('click',(evt)=>{
		this.mouseCoordinates = evt.coordinate;
		if(this.overlay.getPosition()){
			this.overlay.setPosition(undefined);
			this.closer.blur();
		}
	})
	this.selectDoubleClick.on('select',(evt)=>{
		if(evt.target.getFeatures().getLength() > 0){
			var features = evt.target.getFeatures().getArray();
			if(typeof features[0].values_.features !='undefined' && features[0].values_.features.length > 1){
			   var Clustercoordinate = features[0].getGeometry().getCoordinates();
			   var view = self.map.getView();
			   var zoomLevel = view.getZoom();
			   if(zoomLevel < 7){
				   zoomLevel = 7;
			   }else{
				   zoomLevel = zoomLevel +1;
			   }
			   view.setZoom(zoomLevel);
			   view.setCenter(Clustercoordinate);
			}
		}
	})
	this.selectSingleClick.on('select', (evt) =>{
		  if(evt.target.getFeatures().getLength() > 0){
			 var features = evt.target.getFeatures().getArray();
			 if(!(typeof features[0].values_.features != 'undefined' && features[0].values_.features.length > 1)){
				if(features[0].values_ && typeof features[0].values_.features !='undefined'){
					var featuresValues = features[0].values_.features[0].values_;
					if(featuresValues.type=="marker"){
						var coordinate = features[0].getGeometry().getCoordinates();
						 self.content.innerHTML = "";
						 self.graph_loader.style.display="block";
						self.overlay.setPosition(coordinate);
						this.getPopupHtml(coordinate,featuresValues.switches,featuresValues.links);
					}
				}else if(features[0].values_ && typeof(features[0].values_.type)!='undefined' && (features[0].values_.type == 'line' || features[0].values_.type == 'cluster_line')){
					var featuresValues = features[0].values_;
					self.graph_loader.style.display="none";
					self.content.innerHTML = "";
					self.content.innerHTML = this.getIslHtml(featuresValues);
					self.overlay.setPosition(this.mouseCoordinates);
				}
				
			 }				
		 }
	  });
  }

  getStatusOfPops(features){
	  var status = "DISCOVERED";
	  if(features && features.length){
		  features.forEach((f)=>{
			var values = f.values_;
			if(values.status == "FAILED"){
				status = "FAILED";
			}
		  })
	  }
	  return status;
  }
  getPopStatus(switches,links){
	  var state = "DISCOVERED";
	  switches.forEach(s=>{
		  if(s.state =="DEACTIVATED"){
			  state = "FAILED";
		  }
	  })
	  links.forEach(l=>{
		if(l.state =="FAILED"){
			state = "FAILED";
		}
	});
	return state;
  }

  loadMarkersClusters(){
	var self =this;
	if(this.pops && this.pops.length){
		this.pops.forEach((data:any,i)=>{
			var popState = this.getPopStatus(data.switches,data.links);
			this.markers[i]  = new Feature({
				geometry:new Point(proj.fromLonLat([data.location.longitude,data.location.latitude])),
				type:'marker',
				id:data.id,
				status:popState,
				switches:data.switches,
				links:data.links,
				city:data.location.city,
				country:data.location.country

			});
		});
	}
	this.markerSource = new VectorSource({
		features:  self.markers
	  });
	this.clusterSource = new Cluster({
		distance:parseInt(this.clusterDistance, 10),
		source: this.markerSource,
	});
	var vectorLayer= this.clusterLayer = new VectorLayer({
		source: this.clusterSource,
		style: function (feature) {
			var size = feature.get('features').length;
			var style = self.styleCache[size];
			var status = self.getStatusOfPops(feature.get('features'));
			if(size > 1){
				var icon =new Icon({
					src: environment.assetsPath+'/images/yellow.png',
				   	scale: 0.4
				  });
				if(status == "FAILED"){
					icon =new Icon({
						src: environment.assetsPath+'/images/red.png',
						scale: 0.4
					  });
				}				
				if (!style) {
					style = new Style({
					image: icon,
					  text: new Text({
						text: size.toString(),
						fill: new Fill({
						  color: '#000',
						}),
					  }),
					});
					self.styleCache[size] = style;
				  }
			}else{
				var color = "black";
				if(status == 'FAILED'){
					color = 'red';
				}
				style =  new Style({
					image:new CircleStyle({
					  radius: 5,
					  fill: new Fill({color: 'white'}),
					  stroke: new Stroke({
						color: color,
						width: 4,
					  })
					}),
				  });
			}
			return style;	
		  },
	  });
	this.map.addLayer(vectorLayer);
	vectorLayer.setZIndex(5,10);
	setTimeout(()=>{
		this.loadCLusterLinks();
	},500);
	  
  }
 
  getIslHtml(values){
	  var linksData = typeof(values.linksData) !='undefined' ? values.linksData : [];
	  
	  var html="<div class='table-wrapper-scroll-y my-custom-scrollbar'><table  class='table table-bordered table-striped mb-0'><thead><th>Source Switch</th><th>Src Port</th><th>Target Switch</th><th>Dst Port</th><th>Status</th><thead><tbody>";
	  if(linksData.length > 0){
		linksData.forEach(link=>{
			var url = "isl/switch/isl/" + link.source_switch+"/"+link.src_port+"/"+link.target_switch+"/"+link.dst_port;
			html+= "<tr  class='cursor-pointer islLink'><td><a href='"+url+"' target='_blank'>"+link.source_switch_name+"</a></td><td><a href='"+url+"' target='_blank'>"+link.src_port+"</a></td><td><a href='"+url+"' target='_blank'>"+link.target_switch_name+"</a></td><td><a href='"+url+"' target='_blank'>"+link.dst_port+"</a></td><td><a href='"+url+"' target='_blank'>"+link.state+"</a></td></tr>";
		});
	  }else if(values.clusterLinkData && values.clusterLinkData.length){
		  var links = values.clusterLinkData;
		for(var i=0; i < links.length; i++){ 
			var valuesData = links[i].values_.linksData;
			if(valuesData && valuesData.length){
				valuesData.forEach(link=>{
					var url = "isl/switch/isl/" + link.source_switch+"/"+link.src_port+"/"+link.target_switch+"/"+link.dst_port;
	
					html+= "<tr class='cursor-pointer islLink'><td><a href='"+url+"' target='_blank'>"+link.source_switch_name+"</a></td><td><a href='"+url+"' target='_blank'>"+link.src_port+"</a></td><td><a href='"+url+"' target='_blank'>"+link.target_switch_name+"</a></td><td><a href='"+url+"' target='_blank'>"+link.dst_port+"</a></td><td><a href='"+url+"'  target='_blank'>"+link.state+"</a></td></tr>";
				});
			}
		}
	  }
	  html+="</tbody></table></div>";
	  return html;
  }
  getPopupHtml(coordinate,switches,links){	
	  this.graphdata = {nodes:switches,links:links};
      var margin = {top: 10, right: 30, bottom: 30, left: 40},
 	  width = this.content.offsetWidth || 400 - margin.left - margin.right,
	  height = this.content.clientHeight  || 400 - margin.top - margin.bottom;
	  this.topologyGraphService.loadworldMapGraph(this.graphdata,'popup-content',width,height,this.graph_loader);	
  }  

  
  enableLinks(){
	  if(this.clusterSource && this.clusterSource.features && this.clusterSource.features.length){
		
		var LinkArr = [];
		this.clusterSource.features.forEach((f)=>{
			if(f.values_.features.length == 1){
				var featureValues = f.values_.features[0].values_;
				LinkArr.push(featureValues.id);
			}
		});
		if(this.linkSource){
			Object.keys(this.linkSource.uidIndex_).forEach((l)=>{
				var src = this.linkSource.uidIndex_[l].values_.source;
				var dst = this.linkSource.uidIndex_[l].values_.target;
				if(LinkArr.indexOf(src) >= 0 && LinkArr.indexOf(dst) >= 0 ){
					this.linkSource.uidIndex_[l].values_.finished = true;
				}else{
					this.linkSource.uidIndex_[l].values_.finished = false;
				}
			})
			setTimeout(()=>{
				this.linkLayer.getSource().changed();
			},500);
		}
		
	  }
  }
  loadCLusterLinks(){
	  if(this.clusterSource && this.clusterSource.features && this.clusterSource.features.length){
		var clusterFeatures = this.clusterSource.features;
		this.ClusterLinks = [];
		if(this.linkSource){
		for(var i=0; i < clusterFeatures.length; i++){
			var source = clusterFeatures[i];	
			var linkArr = [];
			if(source.values_.features.length > 0){
				var sourceFeatures = source.values_.features;
				sourceFeatures.forEach((sf)=>{
					linkArr.push(sf.values_.id);
				})	
			}
			
			for(var j=0; j < clusterFeatures.length; j++){
			 if(i!=j){
				var target =  clusterFeatures[j];
				var linkTargetArr = [];
				if(target.values_.features.length > 1){
						var targeteFeatures = target.values_.features;
						targeteFeatures.forEach((sf)=>{
							linkTargetArr.push(sf.values_.id);
						})	
					}				
					var hasLink = false;
					var no_of_links = 0;
					var link_status_failed = false;
					var linksData = [];
					Object.keys(this.linkSource.uidIndex_).forEach((l)=>{
						var src = this.linkSource.uidIndex_[l].values_.source;
						var dst = this.linkSource.uidIndex_[l].values_.target;
						var linkStatus = this.linkSource.uidIndex_[l].values_.status;
						if((linkArr.indexOf(src) >= 0 && linkArr.indexOf(dst) >= 0) || (linkTargetArr.indexOf(src) >= 0 && linkTargetArr.indexOf(dst) >= 0) ){
							hasLink = true;
							linksData.push(this.linkSource.uidIndex_[l]);
							var values = this.linkSource.uidIndex_[l].values_;
							no_of_links=no_of_links+parseInt(values.links);
							if(linkStatus == 'FAILED'){
								link_status_failed = true;
							}
						}
					})
					if(hasLink){
						var start_point = source.getGeometry().getCoordinates();
							var end_point = target.getGeometry().getCoordinates();
							var line = new LineString([start_point,end_point]);
						var color = "#00aeff";
						if(link_status_failed){
								color= "#d93923";
							}
							var feature = new Feature({
							geometry: line,
							finished: true,
							type:'cluster_line',
							clusterLinkData:linksData,
							color:color,
							no_links:no_of_links.toString()
							});
							this.ClusterLinks.push(feature);
					}
				
			    }
			}
				
			setTimeout(()=>{
				if(typeof this.clusterLinkLayer !='undefined' && typeof this.clusterLinkLayer.getSource() !='undefined'){
					this.clusterLinkLayer.getSource().clear();	
				 }
				this.clusterLinkSource = new VectorSource({
					features:  this.ClusterLinks
				  });
				this.clusterLinkLayer = new VectorLayer({
					source:this.clusterLinkSource,
					style: function (feature) {
						var no_links = feature.get('no_links');
						var color = feature.get('color');
						return new Style({
							stroke: new Stroke({
							  color: color,
							  width: 2
							}),					
							text: new Text({
								text: no_links,
								font: '10px "Roboto", Helvetica Neue, Helvetica, Arial, sans-serif',
								fill: new Fill({ color: 'black' }),
								stroke: new Stroke({ color: 'black', width: 2 })
							  }),
						  });
					  }
				})
				this.map.addLayer(this.clusterLinkLayer);
				this.clusterLinkLayer.setZIndex(2,10);
			 },100);
		}
	  }
	}
  }
  getLinkObjInPops(srcPop,targetPop){
	  var hasLink = false;
	  var no_of_links = 0;
	  var status = "DISCOVERED";
	  var links = [];
	  this.links.forEach(l=>{
		if(srcPop.switchIds.indexOf(l.source_switch) > -1 && targetPop.switchIds.indexOf(l.target_switch) > -1){
			hasLink = true;
			no_of_links = no_of_links+1;
			links.push(l);
			if(l.state == 'FAILED'){
				status = "FAILED";
			}
		}else if(targetPop.switchIds.indexOf(l.source_switch) > -1 && srcPop.switchIds.indexOf(l.target_switch) > -1){
			hasLink = true;
			no_of_links = no_of_links+1;
			links.push(l);
			if(l.state == 'FAILED'){
				status = "FAILED";
			}
		}
	  });
	  if(hasLink){
		  return {
			  		"source":srcPop.location,
					"target":targetPop.location,
					"no_of_links":no_of_links.toString(),
					"status":status,
					"src":srcPop.id,
					"trgt":targetPop.id,
					"links":links
				};
	  }
	  return {};

  }
  loadLinks(links){
	var self = this;
   	if(links && links.length){
	   links.forEach((link,i)=>{
		  var start_point = proj.transform([link.source.longitude,link.source.latitude], 'EPSG:4326', 'EPSG:3857');
		  var end_point = proj.transform([link.target.longitude,link.target.latitude], 'EPSG:4326', 'EPSG:3857');
		  var line = new LineString([start_point,end_point]);
			 var linksVal = link.no_of_links; 
			 var color = "#00aeff";
			 var status = link.status;
		  if(status =="FAILED"){
			  color= "#d93923";
		  }
            var feature = new Feature({
			  geometry: line,
			  finished: false,
			  type:'line',
			  status:status,
			  links:linksVal,
			  color:color,
			  source:link.src,
			  target:link.trgt,
			  linksData:link.links
            });
           this.linkFeatures.push(feature);
	   });
	   this.addlinks();
   }	
 }

 addlinks(){
	 setTimeout(()=>{
		this.linkSource = new VectorSource({
			features:  this.linkFeatures
		  });
		this.linkLayer = new VectorLayer({
			source:this.linkSource,
			style: function (feature) {
				var color = feature.get('color');				
				var links = feature.get('links');
				if (feature.get('finished')) {
				  return new Style({
					stroke: new Stroke({
					  color: color,
					  width: 2
					}),					
					text: new Text({
						text: links,
						font: '10px "Roboto", Helvetica Neue, Helvetica, Arial, sans-serif',
						fill: new Fill({ color: 'black' }),
						stroke: new Stroke({ color: 'black', width: 2 })
					  })
				  });
				} else {
				  return null;
				}
			  }
		})
		this.map.addLayer(this.linkLayer);
		this.linkLayer.setZIndex(2,10);
	 },100);
 }

}
