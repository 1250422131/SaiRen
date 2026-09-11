#ifndef KONAN_LIBSHARED_H
#define KONAN_LIBSHARED_H
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
typedef bool            libshared_KBoolean;
#else
typedef _Bool           libshared_KBoolean;
#endif
typedef unsigned short     libshared_KChar;
typedef signed char        libshared_KByte;
typedef short              libshared_KShort;
typedef int                libshared_KInt;
typedef long long          libshared_KLong;
typedef unsigned char      libshared_KUByte;
typedef unsigned short     libshared_KUShort;
typedef unsigned int       libshared_KUInt;
typedef unsigned long long libshared_KULong;
typedef float              libshared_KFloat;
typedef double             libshared_KDouble;
typedef float __attribute__ ((__vector_size__ (16))) libshared_KVector128;
typedef void*              libshared_KNativePtr;
struct libshared_KType;
typedef struct libshared_KType libshared_KType;

typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Byte;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Short;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Int;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Long;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Float;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Double;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Char;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Boolean;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Unit;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_UByte;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_UShort;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_UInt;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_ULong;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_tencent_kuikly_core_base_ViewContainer;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Function1;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardView;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewEvent;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Function0;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_tencent_kuikly_core_base_attr_ImageUri;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_collections_List;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_model_Market;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Any;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_model_StockChartTab;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_model_StockChartTab_$serializer;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlinx_serialization_descriptors_SerialDescriptor;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Array;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlinx_serialization_encoding_Decoder;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlinx_serialization_encoding_Encoder;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_model_StockChartTab_Companion;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlinx_serialization_KSerializer;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_imcys_sairen_ui_settings_SettingsStore;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_com_tencent_kuikly_core_pager_Pager;

extern void com_tencent_tmm_knoi_initEnv(void* env, void* value, libshared_KBoolean debug);
extern void com_tencent_tmm_knoi_initBridge();

typedef struct {
  /* Service functions. */
  void (*DisposeStablePointer)(libshared_KNativePtr ptr);
  void (*DisposeString)(const char* string);
  libshared_KBoolean (*IsInstance)(libshared_KNativePtr ref, const libshared_KType* type);
  libshared_kref_kotlin_Byte (*createNullableByte)(libshared_KByte);
  libshared_KByte (*getNonNullValueOfByte)(libshared_kref_kotlin_Byte);
  libshared_kref_kotlin_Short (*createNullableShort)(libshared_KShort);
  libshared_KShort (*getNonNullValueOfShort)(libshared_kref_kotlin_Short);
  libshared_kref_kotlin_Int (*createNullableInt)(libshared_KInt);
  libshared_KInt (*getNonNullValueOfInt)(libshared_kref_kotlin_Int);
  libshared_kref_kotlin_Long (*createNullableLong)(libshared_KLong);
  libshared_KLong (*getNonNullValueOfLong)(libshared_kref_kotlin_Long);
  libshared_kref_kotlin_Float (*createNullableFloat)(libshared_KFloat);
  libshared_KFloat (*getNonNullValueOfFloat)(libshared_kref_kotlin_Float);
  libshared_kref_kotlin_Double (*createNullableDouble)(libshared_KDouble);
  libshared_KDouble (*getNonNullValueOfDouble)(libshared_kref_kotlin_Double);
  libshared_kref_kotlin_Char (*createNullableChar)(libshared_KChar);
  libshared_KChar (*getNonNullValueOfChar)(libshared_kref_kotlin_Char);
  libshared_kref_kotlin_Boolean (*createNullableBoolean)(libshared_KBoolean);
  libshared_KBoolean (*getNonNullValueOfBoolean)(libshared_kref_kotlin_Boolean);
  libshared_kref_kotlin_Unit (*createNullableUnit)(void);
  libshared_kref_kotlin_UByte (*createNullableUByte)(libshared_KUByte);
  libshared_KUByte (*getNonNullValueOfUByte)(libshared_kref_kotlin_UByte);
  libshared_kref_kotlin_UShort (*createNullableUShort)(libshared_KUShort);
  libshared_KUShort (*getNonNullValueOfUShort)(libshared_kref_kotlin_UShort);
  libshared_kref_kotlin_UInt (*createNullableUInt)(libshared_KUInt);
  libshared_KUInt (*getNonNullValueOfUInt)(libshared_kref_kotlin_UInt);
  libshared_kref_kotlin_ULong (*createNullableULong)(libshared_KULong);
  libshared_KULong (*getNonNullValueOfULong)(libshared_kref_kotlin_ULong);

  /* User functions. */
  struct {
    struct {
      struct {
        struct {
          struct {
            struct {
              struct {
                struct {
                  struct {
                    libshared_KType* (*_type)(void);
                    libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardView (*ExpandableInfoCardView)();
                    libshared_kref_kotlin_Function1 (*body)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardView thiz);
                    libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr (*createAttr)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardView thiz);
                    libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewEvent (*createEvent)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardView thiz);
                  } ExpandableInfoCardView;
                  struct {
                    libshared_KType* (*_type)(void);
                    libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr (*ExpandableInfoCardViewAttr)();
                    libshared_kref_kotlin_Function0 (*get_content)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz);
                    void (*set_content)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz, libshared_kref_kotlin_Function0 set);
                    libshared_kref_kotlin_Function0 (*get_expanded)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz);
                    void (*set_expanded)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz, libshared_kref_kotlin_Function0 set);
                    libshared_kref_com_tencent_kuikly_core_base_attr_ImageUri (*get_icon)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz);
                    void (*set_icon)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz, libshared_kref_com_tencent_kuikly_core_base_attr_ImageUri set);
                    const char* (*get_title)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz);
                    void (*set_title)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewAttr thiz, const char* set);
                  } ExpandableInfoCardViewAttr;
                  struct {
                    libshared_KType* (*_type)(void);
                    libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewEvent (*ExpandableInfoCardViewEvent)();
                    libshared_kref_kotlin_Function0 (*get_onToggle)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewEvent thiz);
                    void (*set_onToggle)(libshared_kref_com_imcys_sairen_component_screen_stock_ExpandableInfoCardViewEvent thiz, libshared_kref_kotlin_Function0 set);
                  } ExpandableInfoCardViewEvent;
                  void (*ExpandableInfoCard)(libshared_kref_com_tencent_kuikly_core_base_ViewContainer thiz, libshared_kref_kotlin_Function1 init);
                } stock;
              } screen;
            } component;
            struct {
              struct {
                libshared_KType* (*_type)(void);
                libshared_kref_com_imcys_sairen_model_Market (*Market)(const char* name, const char* params);
                const char* (*get_name)(libshared_kref_com_imcys_sairen_model_Market thiz);
                const char* (*get_params)(libshared_kref_com_imcys_sairen_model_Market thiz);
                const char* (*component1)(libshared_kref_com_imcys_sairen_model_Market thiz);
                const char* (*component2)(libshared_kref_com_imcys_sairen_model_Market thiz);
                libshared_kref_com_imcys_sairen_model_Market (*copy)(libshared_kref_com_imcys_sairen_model_Market thiz, const char* name, const char* params);
                libshared_KBoolean (*equals)(libshared_kref_com_imcys_sairen_model_Market thiz, libshared_kref_kotlin_Any other);
                libshared_KInt (*hashCode)(libshared_kref_com_imcys_sairen_model_Market thiz);
                const char* (*toString)(libshared_kref_com_imcys_sairen_model_Market thiz);
              } Market;
              struct {
                struct {
                  libshared_KType* (*_type)(void);
                  libshared_kref_com_imcys_sairen_model_StockChartTab_$serializer (*_instance)();
                  libshared_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libshared_kref_com_imcys_sairen_model_StockChartTab_$serializer thiz);
                  libshared_kref_kotlin_Array (*childSerializers)(libshared_kref_com_imcys_sairen_model_StockChartTab_$serializer thiz);
                  libshared_kref_com_imcys_sairen_model_StockChartTab (*deserialize)(libshared_kref_com_imcys_sairen_model_StockChartTab_$serializer thiz, libshared_kref_kotlinx_serialization_encoding_Decoder decoder);
                  void (*serialize)(libshared_kref_com_imcys_sairen_model_StockChartTab_$serializer thiz, libshared_kref_kotlinx_serialization_encoding_Encoder encoder, libshared_kref_com_imcys_sairen_model_StockChartTab value);
                } $serializer;
                struct {
                  libshared_KType* (*_type)(void);
                  libshared_kref_com_imcys_sairen_model_StockChartTab_Companion (*_instance)();
                  libshared_kref_kotlinx_serialization_KSerializer (*serializer)(libshared_kref_com_imcys_sairen_model_StockChartTab_Companion thiz);
                } Companion;
                libshared_KType* (*_type)(void);
                libshared_kref_com_imcys_sairen_model_StockChartTab (*StockChartTab)(const char* title, libshared_kref_kotlin_Int scale, libshared_KInt datalen, libshared_KBoolean intraday);
                libshared_KInt (*get_datalen)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                libshared_KBoolean (*get_intraday)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                libshared_kref_kotlin_Int (*get_scale)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                const char* (*get_title)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                const char* (*component1)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                libshared_kref_kotlin_Int (*component2)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                libshared_KInt (*component3)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                libshared_KBoolean (*component4)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                libshared_kref_com_imcys_sairen_model_StockChartTab (*copy)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz, const char* title, libshared_kref_kotlin_Int scale, libshared_KInt datalen, libshared_KBoolean intraday);
                libshared_KBoolean (*equals)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz, libshared_kref_kotlin_Any other);
                libshared_KInt (*hashCode)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
                const char* (*toString)(libshared_kref_com_imcys_sairen_model_StockChartTab thiz);
              } StockChartTab;
              libshared_kref_kotlin_collections_List (*get_marketList)();
              libshared_kref_kotlin_collections_List (*get_stockChartTabList)();
            } model;
            struct {
              struct {
                struct {
                  libshared_KType* (*_type)(void);
                  libshared_kref_com_imcys_sairen_ui_settings_SettingsStore (*SettingsStore)(libshared_kref_com_tencent_kuikly_core_pager_Pager pager);
                } SettingsStore;
              } settings;
            } ui;
          } sairen;
        } imcys;
        struct {
          struct {
            struct {
              void (*initBridge)();
              void (*initEnvExport)(void* env, void* value, libshared_KBoolean debug);
              void (*initialize)();
              void (*preInitEnv)(void* env, libshared_KBoolean debug);
            } knoi;
          } tmm;
        } tencent;
      } com;
      libshared_KInt (*initKuikly)();
    } root;
  } kotlin;
} libshared_ExportedSymbols;
extern libshared_ExportedSymbols* libshared_symbols(void);
#ifdef __cplusplus
}  /* extern "C" */
#endif
#endif  /* KONAN_LIBSHARED_H */
