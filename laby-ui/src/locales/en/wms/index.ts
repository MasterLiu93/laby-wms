/**
 * WMS Module Internationalization File Summary (English)
 */
import warehouse from './warehouse'
import area from './area'
import location from './location'
import category from './category'
import goods from './goods'
import inventory from './inventory'
import inventoryLog from './inventoryLog'
import inventoryWarning from './inventoryWarning'
import inbound from './inbound'
import outbound from './outbound'
import pickingWave from './pickingWave'
import pickingTask from './pickingTask'
import stocktaking from './stocktaking'
import stockMove from './stockMove'
import customer from './customer'
import supplier from './supplier'
import carrier from './carrier'
import home from './home'
import report from './report'

export default {
  wms: {
    ...warehouse,
    ...area,
    ...location,
    ...category,
    ...goods,
    ...inventory,
    ...inventoryLog,
    ...inventoryWarning,
    ...inbound,
    ...outbound,
    ...pickingWave,
    ...pickingTask,
    ...stocktaking,
    ...stockMove,
    ...customer,
    ...supplier,
    ...carrier,
    ...home,
    ...report
  }
}

