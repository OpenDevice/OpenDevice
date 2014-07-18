var app = app || {};


// window.socket = io.connect('http://'+window.location.hostname + ":8181");

window.socket = io.connect("http://localhost:9092");

window.socket.on('connect', function() {
   alert("Conectado!!!");
});
 
// OnReady.
$(document).ready(function() {
	app.mainApp = new app.AppView();	
});


// TODO: Remover metodo de testes...
app.createDemoData = function(){
	app.deviceList.create({name:'Luz DB1', value:0, category: app.DeviceCategory.LAMP});
	app.deviceList.create({name:'Luz DB2', value:1, category: app.DeviceCategory.LAMP});
	app.deviceList.create({name:'Tomada DB1', value:0, category: app.DeviceCategory.POWER_SOURCE});
	app.deviceList.create({name:'Tomada DB2', value:1, category: app.DeviceCategory.POWER_SOURCE});
}


// The Application
// ---------------
app.AppView = Backbone.View.extend({

	// Instead of generating a new element, bind to the existing skeleton of
	// the App already present in the HTML.
	el : '#mws-container',

	// Our template for the line of statistics at the bottom of the app.
	// statsTemplate : _.template($('#stats-template').html()),

	// New
	// Delegated events for creating new items, and clearing completed ones.
	events : {
		// 'keypress #new-todo' : 'createOnEnter',
		// 'click #clear-completed' : 'clearCompleted',
		// 'click #toggle-all' : 'toggleAllComplete'
		'click #btn-device-add' : 'newDevice'
	},

	// At initialization we bind to the relevant events on the `Todos`
	// collection, when items are added or changed. Kick things off by
	// loading any preexisting todos that might be saved in *localStorage*.
	initialize : function() {
		// this.allCheckbox = this.$('#toggle-all')[0];
		// this.$input = this.$('#new-todo');
		// this.$footer = this.$('#footer');
		app.deviceList = new app.DeviceCollection();
		
		this.$deviceList = this.$('#main-device-list');
		
		this.on(app.Constants.ACTION_SEND, this.handleCommandReceived);

		this.listenTo(app.deviceList, 'add', this.addOne);
		this.listenTo(app.deviceList, 'reset', this.addAll);

		// New
		// this.listenTo(app.Todos, 'change:completed', this.filterOne);
		// this.listenTo(app.Todos, 'filter', this.filterAll);
		// this.listenTo(app.Todos, 'all', this.render);

		app.deviceList.fetch();
	},

	// New
	// Re-rendering the App just means refreshing the statistics -- the rest
	// of the app doesn't change.
	render : function() {
//		var completed = app.Todos.completed().length;
//		var remaining = app.Todos.remaining().length;
//
//		if (app.Todos.length) {
//			this.$main.show();
//			this.$footer.show();
//
//			this.$footer.html(this.statsTemplate({
//				completed : completed,
//				remaining : remaining
//			}));
//
//			this.$('#filters li a').removeClass('selected').filter('[href="#/' + (app.TodoFilter || '' ) + '"]').addClass('selected');
//		} else {
//			this.$main.hide();
//			this.$footer.hide();
//		}
//
//		this.allCheckbox.checked = !remaining;
	},

	// Add a single todo item to the list by creating a view for it
	addOne : function(device) {
		var view = new app.DeviceView({model : device});
		this.$deviceList.append(view.render().el);
	},

	// Add all items in the **Device** collection at once.
	addAll : function() {
		this.$deviceList.html('');
		app.deviceList.each(this.addOne, this);
	},

	// New
	filterOne : function(device) {
		device.trigger('visible');
	},

	// New
	filterAll : function() {
		app.Todos.each(this.filterOne, this);
	},

	// New
	// Generate the attributes for a new Todo item.
	newAttributes : function() {
		return {
			title : this.$input.val().trim(),
			order : app.Todos.nextOrder(),
			completed : false
		};
	},

	// New
	// If you hit return in the main input field, create new Todo model,
	// persisting it to localStorage.
	createOnEnter : function(event) {
		if (event.which !== ENTER_KEY || !this.$input.val().trim()) {
			return;
		}

		app.Todos.create(this.newAttributes());
		this.$input.val('');
	},

	// New
	// Clear all completed todo items, destroying their models.
	clearCompleted : function() {
		_.invoke(app.Todos.completed(), 'destroy');
		return false;
	},

	// New
	toggleAllComplete : function() {
		var completed = this.allCheckbox.checked;

		app.Todos.each(function(todo) {
			todo.save({
				'completed' : completed
			});
		});
	},
	
	newDevice : function(){
		$("#new-dialog").dialog({modal: true, buttons: [{
			text: "Cancelar", 
			click: function() {$( this ).dialog( "close" );}},
			{
				text: "Salvar", 
				click: function() {
					app.deviceList.create({name:'Luz Static 1', value:0, category: app.DeviceCategory.LAMP});
					$( this ).dialog( "close" );
			}}]});
	},
	
	/**
	 * 
	 * @param {app.CommandType} type
	 * @param {Number} deviceID
	 * @param {Number} value
	 */
	handleCommandReceived : function(type, deviceID, value){
		console.log('APP : Constants.ACTION_SEND :: handleCommandReceived');
	}
}); 





