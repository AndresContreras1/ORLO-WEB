// Prendas reales para los armarios de prueba. Fotos de prueba de DummyJSON y Fake Store API y de
// Wikimedia Commons (Creative Commons). Mismos códigos de categoría y color que la app.
const DJ = "https://cdn.dummyjson.com/product-images/";
const FS = "https://fakestoreapi.com/img/";
const WM = "https://upload.wikimedia.org/wikipedia/commons/thumb/";

export const PRENDAS = [
  { name: "Camisa de cuadros azul", category: "arriba", color: "azul", url: DJ + "mens-shirts/blue-&-black-check-shirt/thumbnail.webp" },
  { name: "Camisa de cuadros roja", category: "arriba", color: "rojo", url: DJ + "mens-shirts/man-plaid-shirt/thumbnail.webp" },
  { name: "Camisa estampada", category: "arriba", color: "estampado", url: DJ + "mens-shirts/man-short-sleeve-shirt/thumbnail.webp" },
  { name: "Camiseta henley gris", category: "arriba", color: "gris", url: FS + "71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_t.png" },
  { name: "Camiseta azul manga larga", category: "arriba", color: "azul", url: FS + "71YXzeOuslL._AC_UY879_t.png" },
  { name: "Blusa blanca", category: "arriba", color: "blanco", url: FS + "71z3kpMAYsL._AC_UY879_t.png" },
  { name: "Camiseta roja", category: "arriba", color: "rojo", url: FS + "51eg55uWmdL._AC_UX679_t.png" },
  { name: "Jean azul oscuro", category: "abajo", color: "azul", url: WM + "6/6c/NM.0305649.jpg/960px-NM.0305649.jpg" },
  { name: "Short de cuadros", category: "abajo", color: "estampado", url: WM + "1/10/Yellow_Plaid_%283168540848%29.jpg/960px-Yellow_Plaid_%283168540848%29.jpg" },
  { name: "Pantalón negro", category: "abajo", color: "negro", url: WM + "2/29/Byxor_-_Nordiska_museet_-_NM.0103925_%282%29.jpg/960px-Byxor_-_Nordiska_museet_-_NM.0103925_%282%29.jpg" },
  { name: "Chaqueta de algodón beige", category: "abrigo", color: "beige", url: FS + "71li-ujtlUL._AC_UX679_t.png" },
  { name: "Chaqueta negra con capucha", category: "abrigo", color: "negro", url: FS + "81XH0e8fefL._AC_UY879_t.png" },
  { name: "Blazer azul oscuro", category: "abrigo", color: "azul", url: WM + "0/04/Navy_blazer_jacket.jpg/960px-Navy_blazer_jacket.jpg" },
  { name: "Impermeable azul", category: "abrigo", color: "azul", url: FS + "71HblAHs5xL._AC_UY879_-2t.png" },
  { name: "Tenis blancos", category: "calzado", color: "blanco", url: DJ + "mens-shoes/sports-sneakers-off-white-red/thumbnail.webp" },
  { name: "Tenis altos", category: "calzado", color: "rojo", url: DJ + "mens-shoes/nike-air-jordan-1-red-and-black/thumbnail.webp" },
  { name: "Tacones negros", category: "calzado", color: "negro", url: DJ + "womens-shoes/calvin-klein-heel-shoes/thumbnail.webp" },
  { name: "Sandalias negras", category: "calzado", color: "negro", url: DJ + "womens-shoes/black-&-brown-slipper/thumbnail.webp" },
  { name: "Vestido negro", category: "otro", color: "negro", url: DJ + "womens-dresses/corset-with-black-skirt/thumbnail.webp" },
  { name: "Vestido gris", category: "otro", color: "gris", url: DJ + "tops/gray-dress/thumbnail.webp" },
  { name: "Morral azul", category: "accesorio", color: "azul", url: FS + "81fPKd-2AYL._AC_SL1500_t.png" },
  { name: "Bolso negro", category: "accesorio", color: "negro", url: DJ + "womens-bags/women-handbag-black/thumbnail.webp" },
  { name: "Gafas de sol carey", category: "accesorio", color: "cafe", url: DJ + "sunglasses/black-sun-glasses/thumbnail.webp" },
  { name: "Reloj de cuero", category: "accesorio", color: "cafe", url: DJ + "mens-watches/brown-leather-belt-watch/thumbnail.webp" },
];

/** Las prendas de cada armario de prueba (por nombre). */
export const ARMARIOS = {
  andres: ["Camisa de cuadros azul", "Camiseta henley gris", "Camiseta roja", "Jean azul oscuro", "Pantalón negro",
    "Chaqueta de algodón beige", "Chaqueta negra con capucha", "Tenis blancos", "Tenis altos", "Morral azul",
    "Gafas de sol carey", "Reloj de cuero"],
  laura: ["Blusa blanca", "Camisa estampada", "Camiseta azul manga larga", "Jean azul oscuro", "Short de cuadros",
    "Blazer azul oscuro", "Impermeable azul", "Tacones negros", "Sandalias negras", "Vestido negro", "Vestido gris",
    "Bolso negro"],
};
