{
  "global" : {
    "save" : "Guardar",
    "cancel" : "Cancelar",
    "delete" : "Eliminar",
    "required" : "Requerido",
    "download" : "Descargar",
    "link" : "Enlace",
    "bookmark" : "Marcador",
    "close" : "Cerrar",
    "tags" : "Etiquetas"
  },
  "tree" : {
    "subscribe" : "Suscribirse",
    "import" : "Importar",
    "new_category" : "Nueva categoría",
    "all" : "Todos",
    "starred" : "Destacados"
  },
  "subscribe" : {
    "feed_url" : "URL del canal",
	"filtering_expression" : "Expresión de filtrado",
	"filtering_expression_help" : "Si no está vacía, una expresión se evalúa como 'cierta' o 'falsa'. Si es falsa, las nueva entradas de este canal se marcarán como leídas automáticamente.\nLas variables disponibles son 'title' (título), 'content'(contenido), 'url' (URL), 'author' (autor), y 'categories' (categorías) y sus contenidos son convertidos a minúsculas para facilitar la comparación de strings (cadenas de texto).\nEjemplo: url.contains('youtube') or (author eq 'athou' and title.contains('github').\nLa sintaxis completa está disponible <a href='http://commons.apache.org/proper/commons-jexl/reference/syntax.html' target='_blank'>aquí</a>.",
    "feed_name" : "Nombre del canal",
    "category" : "Categoría"
  },
  "import" : {
    "google_reader_prefix" : "Déjame importar tus canales de tu cuenta ",
    "google_reader_suffix" : ".",
    "google_download" : "También puedes subir tu archivo subscriptions.xml.",
    "google_download_link" : "Descárgalo de aquí.",
    "xml_file" : "Archivo OPML"
  },
  "new_category" : {
    "name" : "Nombre",
    "parent" : "Padre"
  },
  "toolbar" : {
    "unread" : "No leídos",
    "all" : "Todos",
    "previous_entry" : "Entrada anterior",
    "next_entry" : "Entrada siguiente",
    "refresh" : "Actualizar",
    "refresh_all" : "Forzar la actualización de todos mis canales.",
    "sort_by_asc_desc" : "Ordenar por fecha asc/desc.",
    "sort_by_abc_zyx" : "Ordenar alfabéticamente",
    "titles_only" : "Sólo títulos",
    "expanded_view" : "Vista expandida",
    "mark_all_as_read" : "Marcar todos como leído",
    "mark_all_older_12_hours" : "Entradas anteriores a 12 horas.",
    "mark_all_older_day" : "Entradas anteriores a un día.",
    "mark_all_older_week" : "Entradas anteriores a una semana.",
    "mark_all_older_two_weeks" : "Entradas anteriores a 2 semanas.",
    "settings" : "Ajustes",
    "profile" : "Perfil",
    "admin" : "Admin",
    "about" : "Acerca de...",
    "logout" : "Cerrar sesión",
    "donate" : "Donar"
  },
  "view" : {
    "entry_source" : "de ",
    "entry_author" : "por ",
    "error_while_loading_feed" : "Error mientras se cargaba este canal.",
    "keep_unread" : "Mantener como no leído.",
    "no_unread_items" : "no tiene entradas sin leer.",
    "mark_up_to_here" : "Marcar como leídos hasta aquí.",
    "search_for" : "buscando:  ",
    "no_search_results" : "No se han encontrado resultados para las palabras clave especificadas."
  },
  "feedsearch" : {
    "hint" : "Introduce una suscripción...",
    "help" : "Usa la tecla Intro para seleccionar y las teclas de flecha para navegar.",
    "result_prefix" : "Tus suscripciones:"
  },
  "settings" : {
    "general" : {
      "value" : "General",
      "language" : "Idioma",
      "language_contribute" : "Contribuye con traducciones.",
      "show_unread" : "Mostrar canales y categorías sin entradas no leídas.",
      "social_buttons" : "Mostrar botones para compartir de redes sociales.",
      "scroll_marks" : "En vista expandida, el desplazamiento por las entradas las marca como leídas."
    },
    "appearance" : "Apariencia",
    "scroll_speed" : "Velocidad de desplazamiento al navegar entre entradas (en milisegundos)",
    "scroll_speed_help" : "ponlo a 0 para desactivarlo",
    "theme" : "Tema",
    "submit_your_theme" : "Envía tu tema ",
    "custom_css" : "CSS personalizado"
  },
  "details" : {
    "feed_details" : "Detalles del canal",
    "url" : "URL",
    "website" : "Sitio web",
    "name" : "Nombre",
    "category" : "Categoría",
    "position" : "Posición",
    "last_refresh" : "Última actualización",
    "message" : "Último mensaje de actualización",
    "next_refresh" : "Próxima actualización",
    "queued_for_refresh" : "En cola para actualizar",
    "feed_url" : "URL del canal",
    "generate_api_key_first" : "Genera una clave API en tu perfil primero.",
    "unsubscribe" : "Terminar suscripción",
    "unsubscribe_confirmation" : "¿Estás seguro de querer terminar tu suscripción a este canal?",
    "delete_category_confirmation" : "¿Estás seguro de querer eliminar esta categoría?",
    "category_details" : "Detalles de la categoría",
    "tag_details" : "Detalles de las etiquetas ",
    "parent_category" : "Categoría principal"
  },
  "profile" : {
    "user_name" : "Nombre de usuario",
    "email" : "Correo electrónico",
    "change_password" : "Cambiar contraseña",
    "confirm_password" : "Confirmar contraseña",
    "minimum_6_chars" : "Mínimo 6 caracteres",
    "passwords_do_not_match" : "Las contraseñas no coinciden",
    "api_key" : "Clave API",
    "api_key_not_generated" : "No generado todavía",
    "generate_new_api_key" : "Generar nueva clave API",
    "generate_new_api_key_info" : "Al cambiar la contraseña se generará una nueva clave API.",
    "opml_export" : "Exportación de OPML",
    "delete_account" : "Eliminar cuenta",
    "delete_account_confirmation" : "¿Eliminar tu cuenta? ¡No habrá vuelta atrás! "
  },
  "about" : {
    "rest_api" : {
      "value" : "REST API",
      "line1" : "CommaFeed está construido sobre JAX-RS y AngularJS. Por lo tanto, una REST API está disponible.",
      "link_to_documentation" : "Enlace a la documentación."
    },
    "keyboard_shortcuts" : "Atajos de teclado",
    "version" : "Versión de CommaFeed",
    "line1_prefix" : "CommaFeed es un proyecto de código abierto. El código se encuentra en ",
    "line1_suffix" : ".",
    "line2_prefix" : "Si encuentras un problema, por favor repórtalo en la página de problemas de ",
    "line2_suffix" : " del proyecto.",
    "line3" : "Si te gusta este proyecto, por favor considera realizar una donación para apoyar al desarrollador y ayudar a cubrir los costes de mantenimiento.",
    "line4" : "Para aquellos de vosotros que prefieran bitcoin, aquí está la dirección ",
    "goodies" : {
      "value" : "Extras",
      "android_app" : "Apps para Android",
      "subscribe_url" : "URL para suscribirse ",
      "chrome_extension" : "Extensión para Chrome.",
      "firefox_extension" : "Extensión para Firefox.",
      "opera_extension" : "Extensón para Opera.",
      "subscribe_bookmarklet" : "Añadir marcador de suscripción (clic).",
      "subscribe_bookmarklet_asc" : "Más antiguos primero",
      "subscribe_bookmarklet_desc" : "Más recientes primero",
      "next_unread_bookmarklet" : "Marcador a la siguiente entrada no leída (arástralo a la barra de marcadores) "
    },
    "translation" : {
      "value" : "Traducción",
      "message" : "Necesitamos tu ayuda para ayudar a traducir CommaFeed.",
      "link" : "Ver cómo contribuir con traducciones."
    },
    "announcements" : "Anuncios",
    "shortcuts" : {
      "mouse_middleclick" : "click medio",
      "open_next_entry" : "abrir la siguiente entrada",
      "open_previous_entry" : "abrir la entrada anterior",
      "spacebar" : "espacio/mayúsculas+espacio",
      "move_page_down_up" : "mueve la página arriba/abajo",
      "focus_next_entry" : "establecer el foco en la siguiente entrada sin abrirla",
      "focus_previous_entry" : "establecer el foco en la entrada anterior sin abrirla",
      "open_next_feed" : "abrir el siguiente canal o categoría",
      "open_previous_feed" : "abrir el canal o categoría previo",
      "open_close_current_entry" : "abrir/cerrar la entrada actual",
      "open_current_entry_in_new_window" : "abrir la entrada actual en una nueva ventana",
      "open_current_entry_in_new_window_background" : "abrir la entrada actual en una nueva ventana en segundo plano",
      "star_unstar" : "destacar la entrada actual",
      "mark_current_entry" : "marcar la entrada actual como leída/no la leída",
      "mark_all_as_read" : "marcar todas las entradas como leídas",
      "open_in_new_tab_mark_as_read" : "abrir entrada en una nueva pestaña y marcar como leída",
      "fullscreen" : "activar/desactivar el modo pantalla completa ",
      "font_size" : "aumentar/reducir el tamaño de la fuente de la entrada actual",
      "go_to_all" : "ver Todos",
      "go_to_starred" : "ver Destacados",
      "feed_search" : "navega a una suscripción al introducir su nombre"
    }
  }
}
