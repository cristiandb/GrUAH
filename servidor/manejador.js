//manejador.js
var geocoderProvider = 'google';
var httpAdapter = 'http';
// optionnal
var extra = {
    formatter: null        
};

var geocoder = require('node-geocoder').getGeocoder(geocoderProvider, httpAdapter, extra);

//BORRAR?????????
function mapas (queryObj,resp,gruas) {
   console.log("Envio de datos a mapas.");
   /* console.log("Envio de datos a mapas.");
	gruas.view_with_list('mapas', 'documentos', 'lista', function(err, lista) {
	  if (err) {
		console.error(err);
	  }
	  else {
		gruas.view('mapas', 'numServicios',{ group: ['true'] }, function(err, servicios) {
		  if (err) {
			console.error(err);
			resp.writeHead(400, {"Content-Type": "text/JSON"});
			resp.write(err);
			resp.end();
		  }
		  else {
		  for (var i in lista) {
			  servicios.rows.forEach(function(doc) {
				if(doc.key==lista[i].id_empleado)
				  lista[i].numero_trabajos=doc.value;
				});
			}
			console.log(lista);
			resp.writeHead(200, {"Content-Type": "text/JSON"});
			resp.write(JSON.stringify(lista));
			resp.end();
			console.log("Localizacion de gruas enviada");
		  }
		});
	  }
	});  */
}

//Inserción de los servicios recibidos de servicios
function gestion (datos,resp,gruas) {
    console.log("Insercion servicio");
	console.log(datos.Direccion);
	//Se obtiene las coordenadas de la dirección del servicio
	geocoder.geocode(datos.Direccion, function(err, res) {
	var loc=[];
	if(!err){
		console.log("Direccion obtenida");
		loc.push({"long": res[0]["longitude"], "lat":res[0]["latitude"]});
		datos.location=loc;
		console.log(datos);
	}else{
		console.log("La direccion no existe");	
		loc.push({"long": 0, "lat":0});
		datos.location=loc;
		console.log(datos);
	}
	gruas.insert(datos,	function(err, body) {
	 if (!err)
	 {
		console.log(body);
		resp.writeHead(200, {"Content-Type": "text/JSON"});
		resp.write(JSON.stringify(body));
		resp.end();
	}
	else{
		var result = [];
		result.push({error: err.error, reason: err.reason});
		console.log(result);
		resp.writeHead(400, {"Content-Type": "text/JSON"});
		resp.write(JSON.stringify(result));
		resp.end();
	 }
	});
	});
}

//Envio de los servicio a bigData
function servicios (datos,resp,gruas) {
console.log("Peticion de bigdata.");
	console.log("Devolver servicios");
	gruas.view_with_list('bigdata', 'documentos', 'lista', function(err, lista) {
	  if (err) {
		console.error(err);
		resp.writeHead(400, {"Content-Type": "text/JSON"});
		resp.write(err);
		resp.end();
	  }
	  else {
		console.log(lista);
		resp.writeHead(200, {"Content-Type": "text/JSON"});
		resp.write(JSON.stringify(lista));
		resp.end();
		console.log("Servicios devueltas");
	 }
	});  
}

//Envio de las posiciones de las gruas a bigData
function posicionGruas (datos,resp,gruas){
console.log("Peticion de bigData.");
	console.log("Devolver localizacion de gruas");
	gruas.view_with_list('mapas', 'documentos', 'lista', function(err, lista) {
	  if (err) {
		console.error(err);
	  }
	  else {
		gruas.view('mapas', 'numServicios',{ group: ['true'] }, function(err, servicios) {
		  if (err) {
			console.error(err);
			resp.writeHead(400, {"Content-Type": "text/JSON"});
			resp.write(err);
			resp.end();
		  }
		  else {
		  for (var i in lista) {
			  servicios.rows.forEach(function(doc) {
				if(doc.key==lista[i].id_empleado)
				  lista[i].numero_trabajos=doc.value;
				});
			}
			console.log(lista);
			resp.writeHead(200, {"Content-Type": "text/JSON"});
			resp.write(JSON.stringify(lista));
			resp.end();
			console.log("Localizacion de gruas enviada a bigData");
		  }
		});
	  }
	});  
}


//SIMULADOR DE SERVICIOS
function login(datos,resp,gruas){
	console.log(datos);
	var result = [];
	result.push({resultado: "ok"});
	resp.writeHead(200, {"Content-Type": "text/JSON"});
		resp.write(JSON.stringify(result));
		resp.end();
}

function registro(datos,resp,gruas){
	console.log(datos);
	var result = [];
	result.push({resultado: "ok"});
	resp.writeHead(200, {"Content-Type": "text/JSON"});
		resp.write(JSON.stringify(result));
		resp.end();
}



exports.mapas = mapas;
exports.gestion = gestion;
exports.servicios=servicios;
exports.posicionGruas=posicionGruas;

exports.login=login;
exports.registro=registro;