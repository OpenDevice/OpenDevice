var app = app || {};

app.CommandType = {
	ON_OFF:1, // Ligar/Deslizar 
	POWER_LEVEL:2, // Controlar Potência
	INFRA_RED:3, // Controle Infra Vermelho//),
	PING_REQUEST:20, // Ping Request,Verificar se esta ativo
	PING_RESPONSE:21, // Ping Response,Resposta para o Ping
	REPORT_DEVICES:30, // Listar Dispositivos
	REPORT_RECEIVED:31 // Comando Recebido
}
