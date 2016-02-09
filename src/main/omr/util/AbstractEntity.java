//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                   A b s t r a c t E n t i t y                                  //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Hervé Bitteur and others 2000-2016. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Class {@code AbstractEntity}
 *
 * @author Hervé Bitteur
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "entity")
public abstract class AbstractEntity
        implements Entity
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(
            AbstractEntity.class);

    //~ Instance fields ----------------------------------------------------------------------------
    //
    // Persistent data
    //----------------
    //
    /** Identity for debugging. */
    @XmlID
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(value = Jaxb.StringIntegerAdapter.class, type = int.class)
    protected int id;

    // Transient data
    //---------------
    //
    /** (Debug) flag this as VIP. */
    protected boolean vip;

    //~ Methods ------------------------------------------------------------------------------------
    //--------//
    // dumpOf //
    //--------//
    @Override
    public String dumpOf ()
    {
        return this.toString(); // By default
    }

    //-------//
    // getId //
    //-------//
    @Override
    public int getId ()
    {
        return id;
    }

    //-------//
    // isVip //
    //-------//
    @Override
    public boolean isVip ()
    {
        return vip;
    }

    //-------//
    // setId //
    //-------//
    @Override
    public void setId (int id)
    {
        this.id = id;
    }

    //--------//
    // setVip //
    //--------//
    @Override
    public void setVip (boolean vip)
    {
        this.vip = vip;
    }

    //----------//
    // toString //
    //----------//
    @Override
    public String toString ()
    {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append(getClass().getSimpleName()).append("{").append("#").append(id);
            sb.append(internals());
            sb.append("}");

            return sb.toString();
        } catch (Throwable ex) {
            logger.warn("toString ex:" + ex, ex);

            return null;
        }
    }

    //-----------//
    // internals //
    //-----------//
    protected String internals ()
    {
        return "";
    }
}