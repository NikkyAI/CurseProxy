using Cursemeta.AddOnService;
using System.Reflection;

namespace Cursemeta.Modpacks {
    public class AddonFileBundle : AddOnFile {
        public AddOn Addon { get; private set; }
        public AddonFileBundle(AddOnFile file, AddOn addon)  {
            Addon = addon;
            foreach ( PropertyInfo oPropertyInfo in file.GetType().GetProperties() )
                {
                    //Check the method is not static
                    if ( !oPropertyInfo.GetGetMethod().IsStatic )
                    {
                        //Check this property can write
                        if ( this.GetType().GetProperty( oPropertyInfo.Name ).CanWrite )
                        {
                            //Check the supplied property can read
                            if ( oPropertyInfo.CanRead )
                            {
                                //Update the properties on this object
                                this.GetType().GetProperty( oPropertyInfo.Name ).SetValue( this, oPropertyInfo.GetValue( file, null ), null );
                            }
                        }
                    }
                }
        }
    }
}