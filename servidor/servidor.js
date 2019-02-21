//Servidor.js
function inicializar ( route, manejador) { //Pasamos el objeto route y el objeto manejador
	var http = require('http');
    var url = require('url');
	var qs = require('querystring');
	var follow = require('follow');
	var server = http.createServer();
	var nano = require('nano')('http://localhost:5984');
	var gruas = nano.db.use('gruas');
	var minutos = 2;
	var intervalo = minutos * 60 * 1000;
	console.log("Conexion couchdb gruas");

	//Función para enviar cambios de estado 
	follow({db:"http://localhost:5984/gruas", include_docs:true, filter:'gestion/cambioEstado',since:'now'}, function(error, change) {
	  if(!error) {
	    if(change.doc.type=="servicio"){
			console.log("Cambio " + change.doc.NumIncidencia + " a " + change.doc.Estado);
			var incidencia=encodeURIComponent(change.doc.NumIncidencia);
			var estado=encodeURIComponent(change.doc.Estado)
			console.log(estado);
			/*var options = {
				host: '172.22.4.142',
				port: '8080',
				path: '/SistemaGestion/rest/incidencias/actualizar?id='+incidencia+'&estado='+estado,
				method: 'GET',
			};*/
			var options = {
				host: 'localhost',
				port: '8888',
				path: '/login?id='+incidencia+'&estado='+estado,
				method: 'GET',
			};
			
			callback = function(response) {
			  var str = '';
			  //Sigue recibiendo datos
			  response.on('data', function (chunk) {
			  console.log("Empezando");
				str += chunk;
			  });
			  //Recibido toda la respuesta
			  response.on('end', function () {
			  console.log("Finalizando");
			  });
			}
			
var req = http.request(options, callback);

req.on('error', function(e) {
  console.log('Error envio datos, posiblemente servidor no disponible');
});

req.end();			
			
		}else{
		console.log("PRUEBA");
			gruas.view('mapas','documentos', function(err, localizaciones) {
				  if (!err) {
				  console.log(change.doc.id_empleado);
						localizaciones.rows.forEach(function(doc) {
						
						  if((change.doc.id_empleado==doc.value.id_empleado)&&(change.doc._id!=doc.value._id)){
						  gruas.destroy(doc.value._id,doc.value._rev , function(err, body) {});
						}
					  });
				}
			});
		}
		}
	  });
	
	
			
	//Función que envia los datos a mapas
	function enviarMapas(){
		console.log("Envio de datos a mapas.");
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
				
				// Build the post string from an object
				var post_data = JSON.stringify(lista);
				console.log(post_data);
				
				
				  // An object of options to indicate where to post to
				 /* var post_options = {
					  host: '172.22.16.190',
					  port: '8084',
					  path: '/Maps/ServicioMapas',
					  method: 'POST',
					  headers: {
						  'Content-Type': 'application/JSON',
						  'Content-Length': post_data.length
					  }
					};*/
					var post_options = {
					  host: 'localhost',
					  port: '8888',
					  path: '/login',
					  method: 'POST',
					  headers: {
						  'Content-Type': 'application/JSON',
						  'Content-Length': post_data.length
					  }
					};
				
				  // Set up the request
				  var post_req = http.request(post_options, function(res) {
					  res.setEncoding('utf8');
					  res.on('data', function (chunk) {
						  console.log('Response: ' + chunk);
					  });
				  })
				  post_req.on('error',function(){
					console.log("Error en el envio a mapas");
				});
				  // post the data
				  post_req.write(post_data);
				  post_req.end();
				console.log("Localizacion de gruas enviada");
			  }
			});
		  }
		});
	}
	
	//Intervalo de envio a mapas
	setInterval(function() {
	 enviarMapas();
	}, intervalo);
	

	//Función que recibe los datos al servidor y redirige la llamada al route
    function control(petic, resp) {
	console.log('Peticion recibida'); //Texto que saldrá por consola
        var pathName = url.parse(petic.url).pathname;
		var datos;
		
		 if(petic.method=='POST') {
            var body='';
            var output = '';
       
        petic.setEncoding('utf8');

        petic.on('data', function (chunk) {
            output += chunk;
        });

        petic.on('end', function() {
            datos = JSON.parse(output);
            
			route(manejador, pathName,datos, resp, gruas); //Además de pathName, pasamos el manejador y resp
        });
		}
		else if(petic.method=='GET') {
				datos = url.parse(petic.url,true);
			route(manejador, pathName,datos, resp, gruas); //Además de pathName, pasamos el manejador y resp
		}
    }
    server.on('request', control).listen(8888);
    console.log('Servidor inicializado');
}
exports.inicializar = inicializar; //Exportamos la función