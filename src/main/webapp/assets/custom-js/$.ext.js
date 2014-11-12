$.expr[":"].containsIgnoreCase = jQuery.expr.createPseudo(function(arg) {
  return function( elem ) {
    return jQuery(elem).text().toLowerCase() === arg.toLowerCase();
  };
});