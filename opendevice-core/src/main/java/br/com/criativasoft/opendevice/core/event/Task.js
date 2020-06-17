// Desligar o ar condicionado automaticamente.

// RUN Todo dia, às 02 da manhã.

if (Time.getHour() > 2 && Time.getHour() < 6) {

    var ar = getDevice('ArCondicionado-Quarto1');

    if (ar.isON()) {

        ar.off();

    }

}