/**
 * System Module Internationalization File Summary (English)
 */
import user from './user'
import role from './role'
import menu from './menu'
import dept from './dept'
import post from './post'
import dict from './dict'
import tenant from './tenant'
import tenantPackage from './tenantPackage'
import sms from './sms'
import mail from './mail'
import notify from './notify'
import notice from './notice'
import operateLog from './operateLog'
import loginLog from './loginLog'
import oauth2 from './oauth2'
import social from './social'
import area from './area'

export default {
  system: {
    ...user,
    ...role,
    ...menu,
    ...dept,
    ...post,
    ...dict,
    ...tenant,
    ...tenantPackage,
    ...sms,
    ...mail,
    ...notify,
    ...notice,
    ...operateLog,
    ...loginLog,
    ...oauth2,
    ...social,
    ...area
  }
}

