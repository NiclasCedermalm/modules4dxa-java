using System.Collections.Generic;
using Tridion.ContentManager;
using Tridion.ContentManager.CommunicationManagement;
using Tridion.ContentManager.Extensibility;
using Tridion.ContentManager.Extensibility.Events;
using Tridion.Logging;

namespace SDL.DXA.Extensions.Container
{
    /// <summary>
    /// Container gravity handler. Finds components on the page that is put on a invalid location by XPM and
    /// correct that. The container framework is based on that components comes in a certain order in the page, i.e.
    /// container component expect that its items comes as next-coming components.
    /// The DXA container framework piggybacks an additional identifier (a container index) in the template ID in the XPM region.
    /// This container index is an aid for this extension to find the correct container for components drag&dropped through XPM.
    /// </summary>
    [TcmExtension("DXA-ContainerGravityHandler")]
    public class ContainerGravityHandler : TcmExtension
    {
        /// <summary>
        /// Constructor
        /// </summary>
                   
        public ContainerGravityHandler()
        {
            EventSystem.Subscribe<Page, SaveEventArgs>(OnPageSave, EventPhases.Initiated);
        }

        /// <summary>
        /// On Page Save.
        /// </summary>
        /// <param name="page"></param>
        /// <param name="args"></param>
        /// <param name="phase"></param>
        public static void OnPageSave(Page page, SaveEventArgs args, EventPhases phase)
        {
            if ( IsContainerPage(page) )
            {
                IList<Container> containers = Container.GetContainers(page);
                //Logger.Write("Regions: " + containers.Count, "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                if (containers.Count > 0)
                {
                    // Check if last component really belongs to the last region, if not let some region gravity happen on it
                    // This is primarily to support adding components on-the-fly in regions via XPM
                    //
                    Container lastContainer = containers[containers.Count - 1];
                    bool foundNewComponent = false;
                    //Logger.Write("Last container : " + lastContainer, "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                    if (lastContainer.ComponentPresentations.Count > 0)
                    {
                        //Logger.Write("Last Region Count:" + lastContainer.ComponentPresentations.Count, "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                        ComponentPresentationInfo lastCP = lastContainer.ComponentPresentations[lastContainer.ComponentPresentations.Count - 1];
                       
                        if (lastCP.ContainerIndex != -1 )
                        {
                            //Logger.Write("Region Index:" + lastCP.ContainerIndex, "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);                       
                            Container container = containers[lastCP.ContainerIndex-1];
                            //Logger.Write("Found container: " + container.Name, "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                            if (container != null)
                            {
                                container.Add(lastCP.ComponentPresentation);

                                // Remove the last entry from the page, because now the CP is added to another region on the page
                                //
                                page.ComponentPresentations.RemoveAt(page.ComponentPresentations.Count - 1);
                                foundNewComponent = true;
                            }
                        }
                    }
                    if (!foundNewComponent)
                    {
                        //Logger.Write("No new component added to the bottom...", "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                        
                        // Process component presentations that have been moved into an empty container
                        //
                        ProcessComponentPresentationsInWrongContainer(page.ComponentPresentations, containers);
                    }
                    ProcessComponentTemplates(page.ComponentPresentations);           
                }

            }
        }

        /// <summary>
        /// Check if page is container page or not.
        /// </summary>
        /// <param name="page"></param>
        /// <returns></returns>
        protected static bool IsContainerPage(Page page)
        {
            foreach (ComponentPresentation cp in page.ComponentPresentations)
            {
                if (Container.IsContainer(cp))
                {
                    return true;
                }
            }
            return false;
        }

        /// <summary>
        /// Process component presentations that are in the wrong container
        /// </summary>
        /// <param name="componentPresentations"></param>
        /// <param name="containers"></param>
        static public void ProcessComponentPresentationsInWrongContainer(IList<ComponentPresentation> componentPresentations, IList<Container> containers)
        {
            int pageIndex = -1;
            bool foundInvalidItem = false;
            Container correctContainer = null;
            ComponentPresentation componentPresentation = null;
            foreach (Container container in containers)
            {
                if ( foundInvalidItem )
                {
                    container.PageIndex--;
                    break;
                }
                pageIndex++; // Count one for each container
                foreach (var cp in container.ComponentPresentations)
                {
                    pageIndex++; // count one for each component presentation
                    if (cp.ContainerIndex != -1 && cp.Owner.Index != cp.ContainerIndex)
                    {
                        Logger.Write("Found invaild item in Container with container index: " + cp.ContainerIndex + " & current container index: " + cp.Owner.Index , "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                        componentPresentations.RemoveAt(pageIndex);
                        correctContainer = containers[cp.ContainerIndex - 1];
                        componentPresentation = cp.ComponentPresentation;
                        foundInvalidItem = true;
                        break; // There should only be one item that is incorrect container
                    }
                }
            }
            if ( correctContainer != null )
            {
                correctContainer.Add(componentPresentation);
            }
        }

        /// <summary>
        /// Process component templates and clean up the component template ID. An additional marker is piggybacked in the ID to indicate the 
        /// container index, which needs to be removed before saving the page.
        /// </summary>
        /// <param name="componentPresentations"></param>
        static public void ProcessComponentTemplates(IList<ComponentPresentation> componentPresentations)
        {
            //Logger.Write("Processing Component Templates...", "ContainernGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
            foreach ( ComponentPresentation cp in componentPresentations )
            {
                // Check if component template really exist -> if not extract the region index from the template ID
                //                       
                if (Container.ExtractContainerIndex(cp.ComponentTemplate.Id) != -1)
                {
                    //Logger.Write("Found Template URI with container index: " + cp.ComponentTemplate.Id, "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                    TcmUri realTemplateUri = Container.RemoveContainerIndex(cp.ComponentTemplate.Id);
                    //Logger.Write("Real Template URI: " + realTemplateUri, "ContainerGravityHandler", LogCategory.Custom, System.Diagnostics.TraceEventType.Information);
                    cp.ComponentTemplate = new ComponentTemplate(realTemplateUri, cp.Session); 
                }

            }
        }
        
    }
}
