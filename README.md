# TPE-POD: Mostradores de Check-in
# Grupo 12

## Instrucciones de Compilación
Para compilar el proyecto, se deben ejecutar los siguientes comandos en la raíz del proyecto:
```bash
chmod +x compile.sh
./compile.sh
```
## Instrucciones de Ejecución
### Servidor
Para ejecutar el servidor, se deben ejecutar los siguientes comandos en la carpeta raíz del proyecto:
```bash
cd ./tmp/tpe1-g12-server-1.0-SNAPSHOT/
./run-server.sh
```

### Admin Client

```bash
cd ./tmp/tpe1-g12-client-1.0-SNAPSHOT/
./admin-client.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -Dsector=sectorName | -Dcounters=counterCount | -DinPath=manifestPath ]
````


### Counter Client

```bash
cd ./tmp/tpe1-g12-client-1.0-SNAPSHOT/
./counter-client.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -Dsector=sectorName | -DcounterFrom=fromVal | -DcounterTo=toVal | -Dflights=flights | -Dairline=airlineName | -DcounterCount=countVal ]
```


### Event Client
```bash
cd ./tmp/tpe1-g12-client-1.0-SNAPSHOT/
./event-client.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dairline=airlineName
```


### Passenger Client
```bash
cd ./tmp/tpe1-g12-client-1.0-SNAPSHOT/
./passenger-client.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -Dbooking=booking | -Dsector=sectorName | -Dcounter=counterNumber ]
```

### Query Client
```bash
cd ./tmp/tpe1-g12-client-1.0-SNAPSHOT/
./query-client.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -DoutPath=query.txt [ -Dsector=sectorName | -Dairline=airlineName | -Dcounter=counterVal ]
```
